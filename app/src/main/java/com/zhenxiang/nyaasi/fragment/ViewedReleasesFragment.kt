package com.zhenxiang.nyaasi.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.AppUtils
import com.zhenxiang.nyaasi.NyaaReleaseActivity
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.ReleasesListAdapter
import com.zhenxiang.nyaasi.db.LocalNyaaDbViewModel
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import dev.chrisbanes.insetter.applyInsetter

open class ViewedReleasesFragment : Fragment() {

    //private lateinit var toolbar: Toolbar
    //private lateinit var searchBar: SearchView
    //private lateinit var searchBtn: ExtendedFloatingActionButton
    lateinit var localNyaaDbViewModel: LocalNyaaDbViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_viewed_releases, container, false)
        /*toolbar = fragmentView.findViewById(R.id.toolbar)
        toolbar.setTitle(getTitleRes())
        searchBar = fragmentView.findViewById(R.id.search_bar)
        searchBtn = fragmentView.findViewById(R.id.search_btn)

        searchBtn.setOnClickListener {
            toolbar.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
            searchBar.isIconified = false
            searchBar.requestFocus()
            searchBtn.hide()
        }

        searchBar.setOnCloseListener {
            hideSearch()
            true
        }*/

        localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        val releasesListAdapter = ReleasesListAdapter()
        releasesListAdapter.setFooterVisible(false)

        liveDataSource().observe(viewLifecycleOwner, {
            releasesListAdapter.setItems(it)
        })

        /*searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchBar.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery(newText)
                return true
            }

        })*/

        val releasesList = fragmentView.findViewById<RecyclerView>(R.id.viewed_releases_list)
        releasesList.applyInsetter {
            type(ime = true) {
                margin()
            }
        }
        releasesList.layoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.adapter = releasesListAdapter
        releasesListAdapter.listener = object : ReleasesListAdapter.ItemClickedListener {
            override fun itemClicked(item: NyaaReleasePreview) {
                NyaaReleaseActivity.startNyaaReleaseActivity(item, requireActivity())
            }

            override fun downloadMagnet(item: NyaaReleasePreview) {
                AppUtils.openMagnetLink(fragmentView.context, item, fragmentView)
            }
        }

        return fragmentView
    }

    open fun liveDataSource(): LiveData<List<NyaaReleasePreview>> {
        return localNyaaDbViewModel.viewedReleases
    }

    open fun searchQuery(query: String?) {
        localNyaaDbViewModel.viewedReleasesSearchFilter.value = query
    }

    /*private fun hideSearch() {
        toolbar.visibility = View.VISIBLE
        searchBar.visibility = View.GONE
        searchBtn.show()
    }*/

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // Check if new state is hidden and if toolbar is visible
        /*if (hidden && toolbar.visibility == View.GONE) {
            hideSearch()
        }*/
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ViewedReleasesFragment().apply {
            }
    }
}