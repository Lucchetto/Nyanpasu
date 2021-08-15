package com.zhenxiang.nyaa.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.*
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.LocalNyaaDbViewModel
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.widget.ReleaseItemAnimator
import dev.chrisbanes.insetter.applyInsetter

open class ViewedReleasesFragment : Fragment(), ReleaseListParent {

    private lateinit var releasesList: RecyclerView
    //private lateinit var toolbar: Toolbar
    //private lateinit var searchBar: SearchView
    //private lateinit var searchBtn: ExtendedFloatingActionButton
    lateinit var localNyaaDbViewModel: LocalNyaaDbViewModel

    private lateinit var fragmentView: View

    private var mQueuedDownload: ReleaseId? = null
    private val permissionRequestLauncher = ReleaseListParent.setupStoragePermissionRequestLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_viewed_releases, container, false)
        val emptyViewHint = fragmentView.findViewById<TextView>(R.id.empty_view)
        emptyViewHint.setText(emptyViewStringRes())
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

        liveDataSource().observe(viewLifecycleOwner, {
            emptyViewHint.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
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

        releasesList = fragmentView.findViewById(R.id.viewed_releases_list)
        releasesList.itemAnimator = ReleaseItemAnimator()
        releasesList.applyInsetter {
            type(ime = true) {
                //margin()
            }
        }

        if (hasDelete()) {
            val swipedCallback = SwipedCallback(releasesList.context, swipeDirection())
            swipedCallback.listener = object: SwipedCallback.ItemDeleteListener {
                override fun onDeleteItem(position: Int) {
                    val releaseToDelete = releasesListAdapter.getItems()[position]
                    localNyaaDbViewModel.removeViewed(releaseToDelete)
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipedCallback)
            itemTouchHelper.attachToRecyclerView(releasesList)
        }
        releasesList.layoutManager = LinearLayoutManager(fragmentView.context)
        releasesList.adapter = releasesListAdapter
        releasesListAdapter.listener = ReleaseListParent.setupReleaseListListener(this)

        return fragmentView
    }

    private fun parentSearchBtn(): View? = activity?.findViewById(R.id.search_btn)

    open fun hasDelete(): Boolean {
        return true
    }

    open fun swipeDirection(): Int {
        return ItemTouchHelper.LEFT
    }

    open fun liveDataSource(): LiveData<List<NyaaReleasePreview>> {
        return localNyaaDbViewModel.viewedReleases
    }

    open fun searchQuery(query: String?) {
        localNyaaDbViewModel.viewedReleasesSearchFilter.value = query
    }

    open fun emptyViewStringRes(): Int {
        return R.string.empty_viewed_releases_view_hint
    }

    fun listHeight(): Int {
        return if (this::releasesList.isInitialized) {
            releasesList.height
        } else {
            0
        }
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

    override fun getQueuedDownload(): ReleaseId? {
        return mQueuedDownload
    }

    override fun getSnackBarParentView(): View {
        return fragmentView
    }

    override fun getSnackBarAnchorView(): View? {
        return parentSearchBtn()
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