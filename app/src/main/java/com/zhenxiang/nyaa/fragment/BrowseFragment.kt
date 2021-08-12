package com.zhenxiang.nyaa.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import android.content.Intent
import android.widget.AdapterView
import android.widget.Spinner
import androidx.recyclerview.widget.ConcatAdapter
import com.zhenxiang.nyaa.*
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaa.AppUtils.Companion.guardDownloadPermission
import com.zhenxiang.nyaa.AppUtils.Companion.storagePermissionForDownloadDenied
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.NyaaApiViewModel
import com.zhenxiang.nyaa.api.ReleaseCategory
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.ReleaseId
import com.zhenxiang.nyaa.util.FooterAdapter
import com.zhenxiang.nyaa.view.BrowsingSpecsSelectorView
import com.zhenxiang.nyaa.view.TitledSpinner

/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: NyaaApiViewModel

    private lateinit var fragmentView: View
    private lateinit var searchBtn: ExtendedFloatingActionButton

    private var waitingDownload: ReleaseId? = null
    private val storagePermissionGuard = createPermissionRequestLauncher {
        waitingDownload?.let { releaseId ->
            if (it) {
                AppUtils.enqueueDownload(releaseId, fragmentView, searchBtn)
            } else {
                storagePermissionForDownloadDenied(fragmentView, searchBtn)
            }
            waitingDownload = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        browseViewModel = ViewModelProvider(this).get(NyaaApiViewModel::class.java)
        if (savedInstanceState == null) {
            browseViewModel.loadResults()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_browse, container, false)

        searchBtn = fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.search_btn)
        searchBtn.setOnClickListener {
            openNyaaSearch()
        }

        val releasesListAdapter = ReleasesListAdapter()
        val footerAdapter = FooterAdapter()
        browseViewModel.resultsLiveData.observe(viewLifecycleOwner,  {
            releasesListAdapter.setItems(it)
            footerAdapter.showLoading(!browseViewModel.endReached())
        })

        val releasesList = fragmentView.findViewById<RecyclerView>(R.id.releases_list)
        val listLayoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.layoutManager = listLayoutManager
        releasesList.adapter = ConcatAdapter(releasesListAdapter, footerAdapter)
        releasesList.itemAnimator = null

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
                AppUtils.openMagnetLink(item, fragmentView, searchBtn)
            }

            override fun downloadTorrent(item: NyaaReleasePreview) {
                guardDownloadPermission(fragmentView.context, storagePermissionGuard, {
                    AppUtils.enqueueDownload(item.id, fragmentView, searchBtn)
                }, {
                    waitingDownload = item.id
                })
            }
        }
        releasesListAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                if (positionStart == 0 && browseViewModel.firstInsert) {
                    releasesList.scrollToPosition(0)
                    browseViewModel.firstInsert = false
                }
            }
        })

        val browsingSpecsSelectorView = fragmentView.findViewById<BrowsingSpecsSelectorView>(R.id.browsing_specs_selector)
        browsingSpecsSelectorView.listener = object: BrowsingSpecsSelectorView.OnSpecsChangedListener {
            override fun releaseCategoryChanged(releaseCategory: ReleaseCategory) {
                browseViewModel.setCategory(releaseCategory)
                browseViewModel.clearResults()
                browseViewModel.loadResults()
            }

            override fun dataSourceChanged(apiDataSource: ApiDataSource) {
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