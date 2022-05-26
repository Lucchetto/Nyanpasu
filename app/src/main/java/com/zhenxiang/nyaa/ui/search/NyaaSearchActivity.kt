package com.zhenxiang.nyaa.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.ReleaseListParent
import com.zhenxiang.nyaa.ReleasesListAdapter
import com.zhenxiang.nyaa.api.*
import com.zhenxiang.nyaa.db.NyaaSearchHistoryItem
import com.zhenxiang.nyaa.db.SearchViewModel
import com.zhenxiang.nyaa.db.SearchHistoryAdapter
import com.zhenxiang.nyaa.ext.collectInLifecycle
import com.zhenxiang.nyaa.model.SearchStatus
import com.zhenxiang.nyaa.util.FooterAdapter
import com.zhenxiang.nyaa.view.BrowsingSpecsSelectorView
import com.zhenxiang.nyaa.widget.SwipedCallback
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch

class NyaaSearchActivity : AppCompatActivity(), ReleaseListParent {

    private val viewModel: SearchViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }

    private lateinit var activityRoot: View
    private lateinit var searchBar: SearchView
    private lateinit var searchSuggestionsContainer: View
    private lateinit var resultsList: RecyclerView
    private var mQueuedDownload: ReleaseId? = null
    private val permissionRequestLauncher =
        ReleaseListParent.setupStoragePermissionRequestLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_search)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        activityRoot = findViewById(R.id.search_activity_root)
        searchBar = findViewById(R.id.search_bar)

        val prefsManager = PreferenceManager.getDefaultSharedPreferences(this)
        if (savedInstanceState == null) {
            searchBar.requestFocus()
        }

        resultsList = findViewById(R.id.search_results)
        if (!NavigationModeUtils.isFullGestures(this@NyaaSearchActivity)) {
            findViewById<CoordinatorLayout>(R.id.search_activity_root).applyInsetter {
                type(navigationBars = true) {
                    margin()
                }
            }
        }
        findViewById<View>(R.id.content).applyInsetter {
            type(ime = true, navigationBars = NavigationModeUtils.isFullGestures(this@NyaaSearchActivity)) {
                padding()
            }
        }
        val resultsAdapter = ReleasesListAdapter()
        val footerAdapter = FooterAdapter()

        val hintText = findViewById<View>(R.id.search_hint)
        searchSuggestionsContainer = findViewById<View>(R.id.suggestions_container)
        val searchSuggestionsTitle = findViewById<View>(R.id.search_suggestions_title)
        val searchSuggestionsList = findViewById<RecyclerView>(R.id.search_suggestions)
        searchSuggestionsList.layoutManager = LinearLayoutManager(this)
        val suggestionsAdapter = SearchHistoryAdapter()
        searchSuggestionsList.adapter = suggestionsAdapter

        // Setup swipe to delete
        val swipedCallback = SwipedCallback(this, ItemTouchHelper.LEFT)
        swipedCallback.listener = object: SwipedCallback.ItemDeleteListener {
            override fun onDeleteItem(position: Int) {
                suggestionsAdapter.getItem(position)?.let {
                    viewModel.delete(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipedCallback)
        itemTouchHelper.attachToRecyclerView(searchSuggestionsList)

        viewModel.searchSpecs.searchQuery = savedInstanceState?.getString(SEARCH_QUERY_TEXT_KEY)
        viewModel.searchHistory.collectInLifecycle(this) {
            if (it.isEmpty()) {
                hintText.visibility = View.VISIBLE
                searchSuggestionsTitle.visibility = View.GONE
                searchSuggestionsList.visibility = View.GONE
            } else {
                hintText.visibility = View.GONE
                searchSuggestionsTitle.visibility = View.VISIBLE
                searchSuggestionsList.visibility = View.VISIBLE
            }
            suggestionsAdapter.updateList(it)
        }
        suggestionsAdapter.listener = object: SearchHistoryAdapter.OnSuggestionActionListener {
            override fun onSuggestionSelected(suggestion: NyaaSearchHistoryItem) {
                searchBar.setQuery(suggestion.searchQuery, true)
            }
        }

        viewModel.resultsFlow.collectInLifecycle(this) {
            resultsAdapter.setItems(it)
        }
        viewModel.searchStatusFlow.collectInLifecycle(this) {
            footerAdapter.showLoading = it != SearchStatus.End
        }
        viewModel.showSuggestionsFlow.collectInLifecycle(this) {
            setShowSuggestions(it)
        }

        val listLayoutManager = LinearLayoutManager(this)
        resultsList.layoutManager = listLayoutManager
        resultsList.adapter = ConcatAdapter(resultsAdapter, footerAdapter)
        resultsList.itemAnimator = null

        resultsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == resultsAdapter.itemCount - 1) {
                    viewModel.nextPage()
                }
            }
        })

        resultsAdapter.listener = ReleaseListParent.setupReleaseListListener(this)
        // Makes sure when items are added on top and recyclerview is on top too, the scroll position isn't changed
        resultsAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                if (positionStart == 0 && itemCount > 0 && !viewModel.hasScrolled) {
                    resultsList.scrollToPosition(0)
                    viewModel.hasScrolled = true
                }
            }
        })
        val browsingSpecsSelectorView = findViewById<BrowsingSpecsSelectorView>(R.id.browsing_specs_selector)
        browsingSpecsSelectorView.listener = object: BrowsingSpecsSelectorView.OnSpecsChangedListener {
            override fun releaseCategoryChanged(releaseCategory: ReleaseCategory) {
                if (viewModel.searchSpecs.category != releaseCategory) {
                    viewModel.searchSpecs.category = releaseCategory
                    if (!viewModel.searchSpecs.searchQuery.isNullOrBlank()) {
                        viewModel.loadResults()
                    }
                }
            }

            override fun dataSourceChanged(apiDataSource: ApiDataSource) {
            }
        }

        if (savedInstanceState == null) {
            browsingSpecsSelectorView.selectDataSource(0)
        }

        searchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            lifecycleScope.launch {
                viewModel.showSuggestionsFlow.emit(
                    hasFocus ||
                            (viewModel.resultsFlow.value.isEmpty() && viewModel.searchStatusFlow.value != SearchStatus.Loading)
                )
            }
        }
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchSpecs.searchQuery = it
                    viewModel.loadResults()
                    viewModel.insert(NyaaSearchHistoryItem(it, System.currentTimeMillis()))

                    searchBar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchHistoryFilter.tryEmit(newText)
                return true
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        searchBar.query?.let {
            outState.putString(SEARCH_QUERY_TEXT_KEY, it.toString())
        }
        super.onSaveInstanceState(outState)
    }

    private fun setShowSuggestions(value: Boolean) {
        if (value) {
            disappearView(resultsList)
            appearView(searchSuggestionsContainer, true)
        } else {
            disappearView(searchSuggestionsContainer, true)
            appearView(resultsList)
        }
    }

    private fun disappearView(view: View, suggestionsAnim: Boolean = false) {
        if (view.visibility == View.GONE) {
            return
        }
        val animation = AnimationUtils.loadAnimation(
            view.context, if (suggestionsAnim) R.anim.suggestions_disappear else R.anim.results_disappear
        )
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        view.startAnimation(animation)
    }

    private fun appearView(view: View, suggestionsAnim: Boolean = false) {
        if (view.visibility == View.VISIBLE && view.animation == null) {
           return
        }
        val animation = AnimationUtils.loadAnimation(
            view.context, if (suggestionsAnim) R.anim.suggestions_appear else R.anim.results_appear
        )
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
        view.startAnimation(animation)
    }

    override fun getQueuedDownload(): ReleaseId? {
        return mQueuedDownload
    }

    override fun getSnackBarParentView(): View {
        return activityRoot
    }

    override fun getSnackBarAnchorView(): View? {
        return null
    }

    override fun setQueuedDownload(releaseId: ReleaseId?) {
        mQueuedDownload = releaseId
    }

    override fun getCurrentActivity(): FragmentActivity {
        return this
    }

    override fun storagePermissionRequestLauncher(): ActivityResultLauncher<String> {
        return permissionRequestLauncher
    }

    companion object {
        private const val SEARCH_QUERY_TEXT_KEY = "search_query_text"
    }
}