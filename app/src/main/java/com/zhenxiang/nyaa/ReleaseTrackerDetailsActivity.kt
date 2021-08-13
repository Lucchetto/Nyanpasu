package com.zhenxiang.nyaa

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaa.api.NyaaApiViewModel
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerRepo
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import com.zhenxiang.nyaa.util.FooterAdapter
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class ReleaseTrackerDetailsActivity : AppCompatActivity() {

    private lateinit var activityRoot: View

    private var queuedDownload: ReleaseId? = null
    private val storagePermissionGuard = createPermissionRequestLauncher {
        queuedDownload?.let { releaseId ->
            if (it) {
                AppUtils.enqueueDownload(releaseId, activityRoot)
            } else {
                AppUtils.storagePermissionForDownloadDenied(activityRoot)
            }
            queuedDownload = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release_tracker_details)
        activityRoot = findViewById(R.id.release_tracker_details_activity_root)

        if (NavigationModeUtils.isFullGestures(this)) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            activityRoot.applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
        }

        val tracker = intent.getSerializableExtra(RELEASE_TRACKER_INTENT_OBJ) as SubscribedTracker?

        tracker?.let { _ ->
            val subscribedTrackerDao = ReleaseTrackerRepo(application).dao
            lifecycleScope.launch(Dispatchers.IO) {
                subscribedTrackerDao.clearNewReleasesCount(tracker.id)
            }

            val title = findViewById<TextView>(R.id.tracker_title)
            val category = findViewById<TextView>(R.id.tracker_category)
            val username = findViewById<TextView>(R.id.tracker_username)
            val latestRelease = findViewById<TextView>(R.id.latest_release_date)
            val trackerCreatedDate = findViewById<TextView>(R.id.tracker_created_date)
            val deleteBtn = findViewById<TextView>(R.id.delete_tracker_btn)
            val latestReleasesList = findViewById<RecyclerView>(R.id.latest_releases_list)

            val searchViewModel = ViewModelProvider(this).get(NyaaApiViewModel::class.java)
            searchViewModel.setCategory(tracker.dataSourceSpecs.category)
            searchViewModel.setSearchText(tracker.searchQuery)
            searchViewModel.setUsername(tracker.username)

            val latestReleasesAdapter = ReleasesListAdapter()
            val footerAdapter = FooterAdapter()
            val listLayoutManager = LinearLayoutManager(this)

            latestReleasesList.layoutManager = listLayoutManager
            latestReleasesList.adapter = ConcatAdapter(latestReleasesAdapter, footerAdapter)
            latestReleasesList.itemAnimator = null
            latestReleasesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (listLayoutManager.findLastVisibleItemPosition() == latestReleasesAdapter.itemCount - 1) {
                        searchViewModel.loadMore()
                    }
                }
            })

            latestReleasesAdapter.listener = object : ReleasesListAdapter.ItemClickedListener {
                override fun itemClicked(item: NyaaReleasePreview) {
                    NyaaReleaseActivity.startNyaaReleaseActivity(item, this@ReleaseTrackerDetailsActivity)
                }

                override fun downloadMagnet(item: NyaaReleasePreview) {
                    AppUtils.openMagnetLink(item, activityRoot)
                }

                override fun downloadTorrent(item: NyaaReleasePreview) {
                    val newDownload = item.getReleaseId()
                    AppUtils.guardDownloadPermission(this@ReleaseTrackerDetailsActivity, storagePermissionGuard, {
                        AppUtils.enqueueDownload(newDownload, activityRoot)
                    }, {
                        queuedDownload = newDownload
                    })
                }
            }

            // Makes sure when items are added on top and recyclerview is on top too, the scroll position isn't changed
            latestReleasesAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    // When items are inserted at the beginning and it's the first insert make sure we jump to the top
                    if (positionStart == 0 && itemCount > 0 && searchViewModel.firstInsert) {
                        latestReleasesList.scrollToPosition(0)
                        searchViewModel.firstInsert = false
                    }
                }
            })

            searchViewModel.resultsLiveData.observe(this, {
                latestReleasesAdapter.setItems(it)
                footerAdapter.showLoading(!searchViewModel.endReached())
            })

            if (savedInstanceState == null) {
                searchViewModel.loadResults()
            }

            category.text = AppUtils.getReleaseCategoryString(this, tracker.dataSourceSpecs.category)
            latestRelease.text = if (tracker.hasPreviousReleases) {
                getString(R.string.tracker_latest_release,
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.latestReleaseTimestamp * 1000))
                )
            } else {
                getString(R.string.tracker_no_releases_yet)
            }
            trackerCreatedDate.text = getString(R.string.tracker_created_on,
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.createdTimestamp))
            )

            if (tracker.searchQuery != null) {
                // First line the query
                title.text = tracker.searchQuery
                // Show username if username is present
                tracker.username?.let {
                    username.text = getString(R.string.tracker_from_user, it)
                    username.visibility = View.VISIBLE
                } ?: run {
                    // Hide username because not available
                    username.text = null
                    username.visibility = View.GONE
                }
            } else if (tracker.username != null) {
                // Username as page title
                title.text = getString(R.string.tracker_all_releases_from_user, tracker.username)
            }

            deleteBtn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    subscribedTrackerDao.deleteById(tracker.id)
                    finish()
                }
            }
        }
    }

    companion object {
        const val RELEASE_TRACKER_INTENT_OBJ = "releaseTracker"

        fun startReleaseTrackerDetailsActivity(tracker: SubscribedTracker, activity: Activity) {
            val intent = Intent(activity, ReleaseTrackerDetailsActivity::class.java).putExtra(
                RELEASE_TRACKER_INTENT_OBJ, tracker)
            activity.startActivity(intent)
        }
    }
}