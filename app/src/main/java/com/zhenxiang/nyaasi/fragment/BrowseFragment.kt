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
import androidx.recyclerview.widget.ConcatAdapter
import com.zhenxiang.nyaasi.*
import com.zhenxiang.nyaasi.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaasi.AppUtils.Companion.guardDownloadPermission
import com.zhenxiang.nyaasi.AppUtils.Companion.storagePermissionForDownloadDenied
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import com.zhenxiang.nyaasi.util.FooterAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: NyaaBrowseViewModel

    private lateinit var fragmentView: View
    private lateinit var searchBtn: ExtendedFloatingActionButton

    private var waitingDownload: Int? = null
    private val storagePermissionGuard = createPermissionRequestLauncher {
        waitingDownload?.let { releaseId ->
            if (it) {
                AppUtils.enqueueDownload(releaseId, fragmentView, searchBtn)
            } else {
                storagePermissionForDownloadDenied(fragmentView, searchBtn)
            }
        }
        waitingDownload = null
    }

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
        fragmentView = inflater.inflate(R.layout.fragment_browse, container, false)

        searchBtn = fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.search_btn)
        searchBtn.setOnClickListener {
            openNyaaSearch()
        }

        val releasesListAdapter = ReleasesListAdapter()
        val footerAdapter = FooterAdapter()
        browseViewModel.itemsLiveData.observe(viewLifecycleOwner,  {
            // Hax until we handle livedata properly
            releasesListAdapter.setItems(it.toList())
            footerAdapter.showLoading(!browseViewModel.isBottomReached())
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
        // Makes sure when items are added on top and recyclerview is on top too, the scroll position isn't changed
        releasesListAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (!releasesList.canScrollVertically(-1)) {
                    releasesList.scrollToPosition(0)
                }
            }
        })

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