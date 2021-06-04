package com.zhenxiang.nyaasi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.api.NyaaSearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NyaaSearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: NyaaSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_search)

        val searchBar = findViewById<SearchView>(R.id.search_bar)
        searchBar.setIconifiedByDefault(false)
        searchBar.requestFocus()

        val resultsAdapter = DownloadsAdapter()
        searchViewModel = ViewModelProvider(this).get(NyaaSearchViewModel::class.java)
        searchViewModel.setSearchText(null)
        searchViewModel.searchResultsLiveData.observe(this,  {
            resultsAdapter.setItems(it)
            resultsAdapter.setFooterVisible(!searchViewModel.isBottomReached())
        })

        val resultsList = findViewById<RecyclerView>(R.id.search_results)
        val listLayoutManager = LinearLayoutManager(this)
        resultsList.layoutManager = listLayoutManager
        resultsList.adapter = resultsAdapter

        // WIP: infinite data loading
        resultsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == resultsAdapter.itemCount - 1) {
                    searchViewModel.loadMore()
                }
            }
        })

        resultsList.addOnItemTouchListener(object: RecyclerViewItemClickListener(resultsList.context) {
            override fun onItemClick(view: View, position: Int) {
                super.onItemClick(view, position)
                resultsAdapter.items.getOrNull(position)?.let {
                    NyaaReleaseActivity.startNyaaReleaseActivity(it, this@NyaaSearchActivity)
                }
            }
        })

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (resultsList.visibility == View.GONE) {
                    resultsList.visibility = View.VISIBLE
                    val hintText = findViewById<View>(R.id.search_hint)
                    hintText.visibility = View.GONE
                }
                searchViewModel.setSearchText(query)
                searchViewModel.loadSearchResults()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }
}