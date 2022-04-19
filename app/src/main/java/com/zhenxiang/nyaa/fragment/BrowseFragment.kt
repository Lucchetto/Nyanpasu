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
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ConcatAdapter
import com.zhenxiang.nyaa.*
import com.zhenxiang.nyaa.api.*
import com.zhenxiang.nyaa.ext.collectInLifecycle
import com.zhenxiang.nyaa.util.FooterAdapter
import com.zhenxiang.nyaa.view.BrowsingSpecsSelectorView
import com.zhenxiang.nyaa.viewmodel.SearchResultsViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [BrowseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrowseFragment : Fragment(), ReleaseListParent {

    private val viewModel: SearchResultsViewModel by viewModels()

    private lateinit var fragmentView: View
    private lateinit var searchBtn: ExtendedFloatingActionButton

    private var mQueuedDownload: ReleaseId? = null
    private val permissionRequestLauncher = ReleaseListParent.setupStoragePermissionRequestLauncher(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_browse, container, false)

        val prefsManager = PreferenceManager.getDefaultSharedPreferences(fragmentView.context)
        val browsingSpecsSelectorView = fragmentView.findViewById<BrowsingSpecsSelectorView>(R.id.browsing_specs_selector)
        if (savedInstanceState == null) {
            browsingSpecsSelectorView.selectDataSource(prefsManager.getInt(
                DEFAULT_SELECTED_DATA_SOURCE, 0))
        }

        searchBtn = fragmentView.findViewById(R.id.search_btn)
        searchBtn.setOnClickListener {
            openNyaaSearch()
        }

        val releasesListAdapter = ReleasesListAdapter()
        val footerAdapter = FooterAdapter()

        viewModel.resultsFlow.collectInLifecycle(this) {
            releasesListAdapter.setItems(it)
        }

        val releasesList = fragmentView.findViewById<RecyclerView>(R.id.releases_list)
        val listLayoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.layoutManager = listLayoutManager
        releasesList.adapter = ConcatAdapter(releasesListAdapter, footerAdapter)
        releasesList.itemAnimator = null

        releasesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listLayoutManager.findLastVisibleItemPosition() == releasesListAdapter.itemCount - 1) {
                    viewModel.nextPage()
                }
            }
        })

        releasesListAdapter.listener = ReleaseListParent.setupReleaseListListener(this)
        releasesListAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                if (positionStart == 0) {
                    releasesList.scrollToPosition(0)
                }
            }
        })

        browsingSpecsSelectorView.listener = object: BrowsingSpecsSelectorView.OnSpecsChangedListener {
            override fun releaseCategoryChanged(releaseCategory: ReleaseCategory) {
                if (viewModel.searchSpecs.category != releaseCategory) {
                    viewModel.searchSpecs.category = releaseCategory
                    viewModel.loadResults()
                }
            }

            override fun dataSourceChanged(apiDataSource: ApiDataSource) {
                prefsManager.edit().putInt(DEFAULT_SELECTED_DATA_SOURCE, apiDataSource.value).apply()
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

        private const val DEFAULT_SELECTED_DATA_SOURCE = "def_selected_data_source"

        // Use this to communicate to host activity
        const val SHOULD_TURN_ON_BYPASS_RESTRICTIONS = "should_turn_on_bypass_restrictions"
    }

    override fun getQueuedDownload(): ReleaseId? {
        return mQueuedDownload
    }

    override fun getSnackBarParentView(): View {
        return fragmentView
    }

    override fun getSnackBarAnchorView(): View {
        return searchBtn
    }

    override fun setQueuedDownload(releaseId: ReleaseId?) {
        mQueuedDownload = releaseId
    }

    override fun storagePermissionRequestLauncher(): ActivityResultLauncher<String> {
        return permissionRequestLauncher
    }

    override fun getCurrentActivity(): FragmentActivity {
        return requireActivity()
    }
}