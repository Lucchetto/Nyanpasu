package com.zhenxiang.nyaasi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.zhenxiang.nyaasi.db.LocalNyaaDbViewModel
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import dev.chrisbanes.insetter.applyInsetter

class ViewedReleasesFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_viewed_releases, container, false)
        val toolbar = fragmentView.findViewById<Toolbar>(R.id.toolbar)
        val searchBar = fragmentView.findViewById<SearchView>(R.id.search_bar)
        val searchBtn = fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.search_btn)
        searchBtn.setOnClickListener {
            toolbar.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
            searchBar.isIconified = false
            searchBar.requestFocus()
            searchBtn.hide()
        }

        searchBar.setOnCloseListener {
            toolbar.visibility = View.VISIBLE
            searchBar.visibility = View.GONE
            searchBtn.show()
            true
        }

        val localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        val releasesListAdapter = ReleasesListAdapter()
        releasesListAdapter.setFooterVisible(false)

        localNyaaDbViewModel.viewedReleases.observe(viewLifecycleOwner, {
            releasesListAdapter.setItems(it)
        })

        searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                localNyaaDbViewModel.searchFilter.value = newText
                return true
            }

        })

        val viewedReleasesList = fragmentView.findViewById<RecyclerView>(R.id.viewed_releases_list)
        viewedReleasesList.applyInsetter {
            type(ime = true) {
                margin()
            }
        }
        viewedReleasesList.layoutManager = LinearLayoutManager(fragmentView.context)
        viewedReleasesList.adapter = releasesListAdapter
        releasesListAdapter.listener = object : ReleasesListAdapter.ItemClickedListener {
            override fun itemClicked(item: NyaaReleasePreview) {
                NyaaReleaseActivity.startNyaaReleaseActivity(item, requireActivity())
            }

            override fun downloadMagnet(item: NyaaReleasePreview) {
                AppUtils.openMagnetLink(fragmentView.context, item, fragmentView, searchBtn)
            }
        }

        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ViewedReleasesFragment().apply {
            }
    }
}