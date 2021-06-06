package com.zhenxiang.nyaasi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.zhenxiang.nyaasi.api.NyaaBrowseViewModel
import android.content.Intent


/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: NyaaBrowseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_browse, container, false)

        val searchBtn = fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.search_btn)
        searchBtn.setOnClickListener {
            openNyaaSearch()
        }

        val releasesListAdapter = ReleasesListAdapter()
        browseViewModel = ViewModelProvider(this).get(NyaaBrowseViewModel::class.java)
        browseViewModel.itemsLiveData.observe(viewLifecycleOwner,  {
            releasesListAdapter.setItems(it)
            releasesListAdapter.setFooterVisible(!browseViewModel.isBottomReached())
        })

        val releasesList = fragmentView.findViewById<RecyclerView>(R.id.releases_list)
        val listLayoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.layoutManager = listLayoutManager
        releasesList.adapter = releasesListAdapter

        // WIP: infinite data loading
        releasesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == releasesListAdapter.itemCount - 1) {
                    browseViewModel.loadMore()
                }
            }
        })

        releasesList.addOnItemTouchListener(object: RecyclerViewItemClickListener(releasesList.context) {
            override fun onItemClick(view: View, position: Int) {
                super.onItemClick(view, position)
                releasesListAdapter.items.getOrNull(position)?.let {
                    NyaaReleaseActivity.startNyaaReleaseActivity(it, requireActivity())
                }
            }
        })

        browseViewModel.loadData()

        return fragmentView
    }

    private fun openNyaaSearch() {
        val intent = Intent(activity, NyaaSearchActivity::class.java)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BrowseFragment().apply {
            }
    }
}