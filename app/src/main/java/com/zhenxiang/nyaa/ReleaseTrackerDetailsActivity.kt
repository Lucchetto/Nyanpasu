package com.zhenxiang.nyaa

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.api.DataSourceViewModel
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerRepo
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import com.zhenxiang.nyaa.releasetracker.SubscribedTrackerFormattedTexts
import com.zhenxiang.nyaa.util.FooterAdapter
import com.zhenxiang.nyaa.view.setTextOrGone
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class ReleaseTrackerDetailsActivity : AppCompatActivity(), ReleaseListParent {

    private lateinit var activityRoot: View

    private var mQueuedDownload: ReleaseId? = null
    private val permissionRequestLauncher = ReleaseListParent.setupStoragePermissionRequestLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release_tracker_details)
        activityRoot = findViewById(R.id.release_tracker_details_activity_root)

        val latestReleasesList = findViewById<RecyclerView>(R.id.latest_releases_list)
        if (NavigationModeUtils.isFullGestures(this)) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            activityRoot.applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
            latestReleasesList.applyInsetter {
                type(navigationBars = true) {
                    padding()
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
            val subtitle = findViewById<TextView>(R.id.tracker_subtitle)
            val categoryAndDataSource = findViewById<TextView>(R.id.tracker_category)
            val username = findViewById<TextView>(R.id.tracker_source_username)
            val latestRelease = findViewById<TextView>(R.id.latest_release_date)
            val trackerCreatedDate = findViewById<TextView>(R.id.tracker_created_date)
            val deleteBtn = findViewById<TextView>(R.id.delete_tracker_btn)

            val searchViewModel = ViewModelProvider(this).get(DataSourceViewModel::class.java)
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

            latestReleasesAdapter.listener = ReleaseListParent.setupReleaseListListener(this)

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

            trackerCreatedDate.text = getString(R.string.tracker_created_on,
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.createdTimestamp))
            )
            val formattedTexts = SubscribedTrackerFormattedTexts.fromTracker(tracker, this)

            title.setTextOrGone(formattedTexts.title)
            subtitle.setTextOrGone(formattedTexts.subtitle)
            categoryAndDataSource.setTextOrGone(formattedTexts.categoryAndDataSource)
            username.setTextOrGone(formattedTexts.username)
            latestRelease.setTextOrGone(formattedTexts.latestRelease)

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

    override fun getQueuedDownload(): ReleaseId? {
        return mQueuedDownload
    }

    override fun getSnackBarParentView(): View {
        return activityRoot
    }

    override fun getSnackBarAnchorView(): View? {
        return null
    }

    override fun setQueuedDownload(releaseId: ReleaseId?) {
        mQueuedDownload = releaseId
    }

    override fun storagePermissionRequestLauncher(): ActivityResultLauncher<String> {
        return permissionRequestLauncher
    }

    override fun getCurrentActivity(): FragmentActivity {
        return this
    }
}