package com.zhenxiang.nyaasi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.api.NyaaSearchViewModel
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import dev.chrisbanes.insetter.applyInsetter

class NyaaSearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: NyaaSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_search)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val searchBar = findViewById<SearchView>(R.id.search_bar)
        searchBar.setIconifiedByDefault(false)
        searchBar.requestFocus()

        val resultsList = findViewById<RecyclerView>(R.id.search_results)
        findViewById<CoordinatorLayout>(R.id.search_activity_root).applyInsetter {
            type(navigationBars = !NavigationModeUtils.isFullGestures(this@NyaaSearchActivity), statusBars = true, ime = true) {
                margin()
            }
        }
        val resultsAdapter = ReleasesListAdapter()
        searchViewModel = ViewModelProvider(this).get(NyaaSearchViewModel::class.java)

        searchViewModel.searchResultsLiveData.observe(this,  {
            if (it.size > 0 && resultsList.visibility == View.GONE) {
                resultsList.visibility = View.VISIBLE
                val hintText = findViewById<View>(R.id.search_hint)
                hintText.visibility = View.GONE
            }
            // Hax until we handle livedata properly
            resultsAdapter.setItems(it.toList())
            resultsAdapter.setFooterVisible(!searchViewModel.isBottomReached())
        })
        if (savedInstanceState == null) {
            searchViewModel.setSearchText(null)
        }

        val listLayoutManager = LinearLayoutManager(this)
        resultsList.layoutManager = listLayoutManager
        resultsList.adapter = resultsAdapter
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
                AppUtils.openMagnetLink(this@NyaaSearchActivity, item, resultsList)
            }
        }

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
                if (resultsList.visibility == View.GONE) {
                    resultsList.visibility = View.VISIBLE
                    val hintText = findViewById<View>(R.id.search_hint)
                    hintText.visibility = View.GONE
                }
                searchViewModel.setSearchText(query)
                searchViewModel.loadSearchResults()
                searchBar.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }
}