package com.zhenxiang.nyaasi

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaasi.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.api.NyaaSearchViewModel
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import com.zhenxiang.nyaasi.db.NyaaSearchHistoryItem
import com.zhenxiang.nyaasi.db.NyaaSearchHistoryViewModel
import com.zhenxiang.nyaasi.util.FooterAdapter
import dev.chrisbanes.insetter.applyInsetter

class NyaaSearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: NyaaSearchViewModel
    private lateinit var searchHistoryViewModel: NyaaSearchHistoryViewModel

    private lateinit var activityRoot: View
    private var waitingDownload: Int? = null
    private val storagePermissionGuard = createPermissionRequestLauncher {
        waitingDownload?.let { releaseId ->
            if (it) {
                AppUtils.enqueueDownload(releaseId, activityRoot)
            } else {
                AppUtils.storagePermissionForDownloadDenied(activityRoot)
            }
        }
        waitingDownload = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_search)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        activityRoot = findViewById(R.id.search_activity_root)

        val searchBar = findViewById<SearchView>(R.id.search_bar)
        searchBar.setSearchableInfo((getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName))

        val resultsList = findViewById<RecyclerView>(R.id.search_results)
        findViewById<CoordinatorLayout>(R.id.search_activity_root).applyInsetter {
            type(navigationBars = !NavigationModeUtils.isFullGestures(this@NyaaSearchActivity), statusBars = true, ime = true) {
                margin()
            }
        }
        val resultsAdapter = ReleasesListAdapter()
        val footerAdapter = FooterAdapter()
        searchViewModel = ViewModelProvider(this).get(NyaaSearchViewModel::class.java)
        searchHistoryViewModel = ViewModelProvider(this).get(NyaaSearchHistoryViewModel::class.java)

        searchViewModel.searchResultsLiveData.observe(this, {
            if (it.size > 0 && resultsList.visibility == View.GONE) {
                resultsList.visibility = View.VISIBLE
                val hintText = findViewById<View>(R.id.search_hint)
                hintText.visibility = View.GONE
            }
            resultsAdapter.setItems(it)
            footerAdapter.showLoading(!searchViewModel.endReached())
        })
        if (savedInstanceState == null) {
            searchViewModel.setSearchText(null)
            searchBar.requestFocus()
        }

        /*val searchSuggestionsAdapter = SimpleCursorAdapter(searchBar.context, android.R.layout.simple_list_item_1, null, arrayOf("searchQuery"), intArrayOf(android.R.id.text1), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        lifecycleScope.launch(Dispatchers.IO) {
            val newCursor = searchHistoryViewModel.getSearchCursor()
            withContext(Dispatchers.Main) {
                searchSuggestionsAdapter.changeCursor(newCursor)
            }
        }
        searchBar.suggestionsAdapter = searchSuggestionsAdapter
        searchBar.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                return false
            }

        })*/
        val suggestionsAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        searchHistoryViewModel.searchHistory.observe(this, {
            suggestionsAdapter.clear()
            suggestionsAdapter.addAll(it.map { item -> item.searchQuery })
        })
        val searchBarTextField = searchBar.findViewById<AutoCompleteTextView>(androidx.appcompat.R.id.search_src_text)
        searchBarTextField.threshold = 0
        searchBarTextField.setAdapter(suggestionsAdapter)
        searchBarTextField.setOnItemClickListener { parent, view, position, id ->
            searchBar.setQuery(suggestionsAdapter.getItem(position), true)
        }

        /*searchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                //searchBar.suggestionsAdapter = searchSuggestionsAdapter
            }
        }*/

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

        resultsAdapter.listener = object : ReleasesListAdapter.ItemClickedListener {
            override fun itemClicked(item: NyaaReleasePreview) {
                NyaaReleaseActivity.startNyaaReleaseActivity(item, this@NyaaSearchActivity)
            }

            override fun downloadMagnet(item: NyaaReleasePreview) {
                AppUtils.openMagnetLink(item, activityRoot)
            }

            override fun downloadTorrent(item: NyaaReleasePreview) {
                AppUtils.guardDownloadPermission(this@NyaaSearchActivity, storagePermissionGuard, {
                    AppUtils.enqueueDownload(item.id, activityRoot)
                }, {
                    waitingDownload = item.id
                })
            }
        }
        // Makes sure when items are added on top and recyclerview is on top too, the scroll position isn't changed
        resultsAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                if (positionStart == 0 && searchViewModel.firstInsert) {
                    resultsList.scrollToPosition(0)
                    searchViewModel.firstInsert = false
                }
            }
        })

        val categoriesSpinner = findViewById<Spinner>(R.id.categories_selection)
        categoriesSpinner.adapter = AppUtils.getNyaaCategoriesSpinner(this)
        // Prevent listener from firing on start
        categoriesSpinner.setSelection(0, false)
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                searchViewModel.setCategory(NyaaReleaseCategory.values()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (resultsList.visibility == View.GONE) {
                        resultsList.visibility = View.VISIBLE
                        val hintText = findViewById<View>(R.id.search_hint)
                        hintText.visibility = View.GONE
                    }
                    // Clear results so it will show loading status
                    searchViewModel.clearResults()
                    searchViewModel.setSearchText(it)
                    searchViewModel.loadSearchResults()
                    searchHistoryViewModel.insert(NyaaSearchHistoryItem(it, System.currentTimeMillis()))
                    searchBar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                /*lifecycleScope.launch(Dispatchers.IO) {
                    Log.w("asdsasd", newText.toString())
                    val newCursor = searchHistoryViewModel.getSearchCursor(newText)
                    withContext(Dispatchers.Main) {
                        //searchBar.suggestionsAdapter = SimpleCursorAdapter(searchBar.context, android.R.layout.simple_list_item_1, newCursor, arrayOf("searchQuery"), intArrayOf(android.R.id.text1), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
                    }
                }*/
                return false
            }

        })
    }
}