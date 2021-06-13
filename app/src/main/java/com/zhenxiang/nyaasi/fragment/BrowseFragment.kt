package com.zhenxiang.nyaasi.fragment

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
import android.widget.AdapterView
import android.widget.Spinner
import com.zhenxiang.nyaasi.*
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.db.NyaaReleasePreview


/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: NyaaBrowseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        browseViewModel = ViewModelProvider(this).get(NyaaBrowseViewModel::class.java)
        if (savedInstanceState == null) {
            browseViewModel.loadData()
        }
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
        browseViewModel.itemsLiveData.observe(viewLifecycleOwner,  {
            releasesListAdapter.setItems(it)
            releasesListAdapter.setFooterVisible(!browseViewModel.isBottomReached())
        })

        val releasesList = fragmentView.findViewById<RecyclerView>(R.id.releases_list)
        val listLayoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.layoutManager = listLayoutManager
        releasesList.adapter = releasesListAdapter

        releasesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == releasesListAdapter.itemCount - 1) {
                    browseViewModel.loadMore()
                }
            }
        })

        releasesListAdapter.listener = object : ReleasesListAdapter.ItemClickedListener {
            override fun itemClicked(item: NyaaReleasePreview) {
                NyaaReleaseActivity.startNyaaReleaseActivity(item, requireActivity())
            }

            override fun downloadMagnet(item: NyaaReleasePreview) {
                AppUtils.openMagnetLink(fragmentView.context, item, fragmentView, searchBtn)
            }
        }

        val categoriesSpinner = fragmentView.findViewById<Spinner>(R.id.categories_selection)
        categoriesSpinner.adapter = AppUtils.getNyaaCategoriesSpinner(fragmentView.context)

        // Prevent listener from firing on start
        categoriesSpinner.setSelection(0, false)
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                browseViewModel.setCategory(NyaaReleaseCategory.values()[position])
                browseViewModel.loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

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