package com.zhenxiang.nyaa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.api.*
import com.zhenxiang.nyaa.db.NyaaSearchHistoryItem
import com.zhenxiang.nyaa.db.NyaaSearchHistoryViewModel
import com.zhenxiang.nyaa.db.SearchHistoryAdapter
import com.zhenxiang.nyaa.util.FooterAdapter
import com.zhenxiang.nyaa.view.BrowsingSpecsSelectorView
import com.zhenxiang.nyaa.widget.SwipedCallback
import dev.chrisbanes.insetter.applyInsetter

class NyaaSearchActivity : AppCompatActivity(), ReleaseListParent {

    private lateinit var searchViewModel: DataSourceViewModel
    private lateinit var searchHistoryViewModel: NyaaSearchHistoryViewModel

    private lateinit var activityRoot: View
    private lateinit var searchSuggestionsContainer: View
    private lateinit var resultsList: RecyclerView
    private var mQueuedDownload: ReleaseId? = null
    private val permissionRequestLauncher = ReleaseListParent.setupStoragePermissionRequestLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_search)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        activityRoot = findViewById(R.id.search_activity_root)

        val prefsManager = PreferenceManager.getDefaultSharedPreferences(this)
        val searchBar = findViewById<SearchView>(R.id.search_bar)

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
        searchViewModel = ViewModelProvider(this).get(DataSourceViewModel::class.java)
        searchHistoryViewModel = ViewModelProvider(this).get(NyaaSearchHistoryViewModel::class.java)

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
                    searchHistoryViewModel.delete(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipedCallback)
        itemTouchHelper.attachToRecyclerView(searchSuggestionsList)

        searchHistoryViewModel.searchHistory.observe(this, {
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
        })
        suggestionsAdapter.listener = object: SearchHistoryAdapter.OnSuggestionActionListener {
            override fun onSuggestionSelected(suggestion: NyaaSearchHistoryItem) {
                searchBar.setQuery(suggestion.searchQuery, true)
            }
        }

        searchViewModel.resultsLiveData.observe(this, {
            resultsAdapter.setItems(it)
            footerAdapter.showLoading(!searchViewModel.endReached())
        })
        searchViewModel.setupRegionalBlockDetection(this, this, prefsManager)

        // Handle saved instance or new instance
        if (savedInstanceState == null) {
            searchViewModel.setSearchText(null)
            searchBar.requestFocus()
        } else {
            setShowSuggestions(searchBar.hasFocus() || searchViewModel.firstInsert)
        }

        val listLayoutManager = LinearLayoutManager(this)
        resultsList.layoutManager = listLayoutManager
        resultsList.adapter = ConcatAdapter(resultsAdapter, footerAdapter)
        resultsList.itemAnimator = null

        resultsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == resultsAdapter.itemCount - 1) {
                    searchViewModel.loadMore()
                }
            }
        })

        resultsAdapter.listener = ReleaseListParent.setupReleaseListListener(this)
        // Makes sure when items are added on top and recyclerview is on top too, the scroll position isn't changed
        resultsAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                if (positionStart == 0 && itemCount > 0 && searchViewModel.firstInsert) {
                    resultsList.scrollToPosition(0)
                    searchViewModel.firstInsert = false
                }
            }
        })

        val browsingSpecsSelectorView = findViewById<BrowsingSpecsSelectorView>(R.id.browsing_specs_selector)
        browsingSpecsSelectorView.selectDataSource(0)
        browsingSpecsSelectorView.listener = object: BrowsingSpecsSelectorView.OnSpecsChangedListener {
            override fun releaseCategoryChanged(releaseCategory: ReleaseCategory) {
                searchViewModel.setCategory(releaseCategory)
            }

            override fun dataSourceChanged(apiDataSource: ApiDataSource) {
            }
        }

        searchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            setShowSuggestions(hasFocus || searchViewModel.firstInsert)
        }
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Clear results so it will show loading status
                    searchViewModel.clearResults()
                    searchViewModel.setSearchText(it)
                    searchViewModel.loadResults()
                    searchHistoryViewModel.insert(NyaaSearchHistoryItem(it, System.currentTimeMillis()))

                    searchBar.clearFocus()
                    setShowSuggestions(false)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchHistoryViewModel.searchHistoryFilter.value = newText
                return true
            }

        })
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
}