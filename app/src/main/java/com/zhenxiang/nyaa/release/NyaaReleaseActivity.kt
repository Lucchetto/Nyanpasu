package com.zhenxiang.nyaa.release

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaa.BuildConfig
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.ReleaseListParent
import com.zhenxiang.nyaa.api.CommentsAdapter
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.LocalNyaaDbViewModel
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.fragment.ReleaseTrackerBottomFragment
import com.zhenxiang.nyaa.fragment.ReleaseTrackerFragmentSharedViewModel
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaa.view.ReleaseDataItemView
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*
import androidx.recyclerview.widget.LinearSmoothScroller
import com.zhenxiang.nyaa.view.MarkdownWebView

class NyaaReleaseActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    private lateinit var coordinatorRoot: View
    private lateinit var markdownView: MarkdownWebView
    private lateinit var submitter: TextView
    private lateinit var manageTrackerBtn: Button

    private lateinit var commentsBackToTop: FloatingActionButton
    private lateinit var commentsSheetBehaviour: BottomSheetBehavior<View>
    private lateinit var commentsSortingOptions: ArrayAdapter<String>

    private lateinit var releasesTrackerViewModel: ReleaseTrackerViewModel
    private lateinit var releaseDetails: ReleaseDetailsHolderViewModel
    private var latestRelease: NyaaReleasePreview? = null

    private var queuedDownload: ReleaseId? = null
    private val storagePermissionGuard = createPermissionRequestLauncher { granted ->
        queuedDownload?.let {
            if (granted) {
                AppUtils.enqueueDownload(it, coordinatorRoot)
            } else {
                AppUtils.storagePermissionForDownloadDenied(coordinatorRoot)
            }
            queuedDownload = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)

        coordinatorRoot = findViewById(R.id.coordinator_root)

        if (NavigationModeUtils.isFullGestures(coordinatorRoot.context)) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            coordinatorRoot.applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
        }

        markdownView = findViewById(R.id.release_details_markdown)
        markdownView.clipToOutline = true
        submitter = findViewById(R.id.submitter)

        commentsSheetBehaviour =
            (findViewById<View>(R.id.comments_sheet).layoutParams as CoordinatorLayout.LayoutParams)
                .behavior as BottomSheetBehavior<View>
        if (savedInstanceState == null) {
            commentsSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }
        commentsBackToTop = findViewById(R.id.comments_back_to_top)
        // Hax to hide fab while laying out the view already
        if (savedInstanceState == null || savedInstanceState.getBoolean("goBackFabHidden", true)) {
            commentsBackToTop.scaleX = 0f
            commentsBackToTop.scaleY = 0f
        }
        commentsSortingOptions = ArrayAdapter(this, R.layout.spinner_dropdown_activatable_item,
            resources.getStringArray(R.array.comments_sorting_order))

        val nyaaRelease = intent.getSerializableExtra(RELEASE_PREVIEW_INTENT_OBJ) as NyaaReleasePreview?
        latestRelease = savedInstanceState?.getSerializable(USER_LATEST_RELEASE_INTENT_OBJ) as NyaaReleasePreview?

        val localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        val releaseTrackerFragmentSharedViewModel = ViewModelProvider(this).get(ReleaseTrackerFragmentSharedViewModel::class.java)
        releaseDetails = ViewModelProvider(this).get(ReleaseDetailsHolderViewModel::class.java)

        nyaaRelease?.let {

            manageTrackerBtn = findViewById(R.id.add_to_tracker)
            releaseTrackerFragmentSharedViewModel.currentUserTracked.observe(this) {
                setButtonTracked(it)
            }

            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name

            val idView = findViewById<TextView>(R.id.release_id)
            idView.text = getString(R.string.release_id_content, it.number.toString(), it.dataSourceSpecs.source.url)

            val openLinkBtn = findViewById<View>(R.id.open_link_btn)
            openLinkBtn.setOnClickListener { _ ->
                openReleaseLink(it)
            }
            val magnetBtn = findViewById<View>(R.id.magnet_btn)
            magnetBtn.setOnClickListener { _ ->
                AppUtils.openMagnetLink(it, coordinatorRoot)
            }
            magnetBtn.setOnLongClickListener { _ ->
                ReleaseListParent.copyToClipboardShowSnackbar(
                    it.name, it.magnet,
                    getString(R.string.magnet_link_copied), coordinatorRoot, null
                )
                true
            }

            val downloadBtn = findViewById<View>(R.id.download_btn)
            downloadBtn.setOnClickListener { _ ->
                val newDownload = it.getReleaseId()
                AppUtils.guardDownloadPermission(this, storagePermissionGuard, {
                    AppUtils.enqueueDownload(newDownload, coordinatorRoot)
                }, {
                    queuedDownload = newDownload
                })
            }

            downloadBtn.setOnLongClickListener { _ ->
                ReleaseListParent.copyToClipboardShowSnackbar(
                    it.name,
                    AppUtils.getReleaseTorrentUrl(it.getReleaseId(), false),
                    getString(R.string.torrent_link_copied), coordinatorRoot, null
                )
                true
            }

            val category = findViewById<TextView>(R.id.category)
            category.text = AppUtils.getReleaseCategoryString(this, it.dataSourceSpecs.category)

            val date = findViewById<TextView>(R.id.date)
            date.text = getString(
                R.string.release_date,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(it.timestamp * 1000)))

            val seeders = findViewById<ReleaseDataItemView>(R.id.seeders)
            seeders.setValue(it.seeders.toString())

            val leechers = findViewById<ReleaseDataItemView>(R.id.leechers)
            leechers.setValue(it.leechers.toString())

            val completed = findViewById<ReleaseDataItemView>(R.id.completed)
            completed.setValue(it.completed.toString())

            val releaseSizeView = findViewById<ReleaseDataItemView>(R.id.release_size)
            releaseSizeView.setValue(it.releaseSize)

            val commentsSection = findViewById<View>(R.id.comments_section)
            val commentsViewAll = findViewById<View>(R.id.show_all_comments)
            val commentsCount = findViewById<TextView>(R.id.comments_count)
            commentsSection.setOnClickListener {
                commentsSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
            commentsViewAll.setOnClickListener {
                commentsSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }

            val commentsList = findViewById<RecyclerView>(R.id.comments_list)

            if (NavigationModeUtils.isFullGestures(coordinatorRoot.context)) {
                commentsList.applyInsetter {
                    type(navigationBars = true) {
                        padding()
                    }
                }
                commentsSection.applyInsetter {
                    type(navigationBars = true) {
                        margin()
                    }
                }
                commentsBackToTop.applyInsetter {
                    type(navigationBars = true) {
                        margin()
                    }
                }
            }

            val commentsListLayoutManager = LinearLayoutManager(this)
            val commentsListAdapter = CommentsAdapter()
            commentsListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            commentsList.layoutManager = commentsListLayoutManager
            commentsList.adapter = commentsListAdapter

            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            commentsList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (commentsListLayoutManager.findFirstVisibleItemPosition() == 0) {
                        commentsBackToTop.hide()
                    } else {
                        commentsBackToTop.show()
                    }
                }
            })

            commentsBackToTop.setOnClickListener {
                smoothScroller.targetPosition = 0
                commentsListLayoutManager.startSmoothScroll(smoothScroller)
            }

            val commentsSheetToolbar = findViewById<Toolbar>(R.id.comments_sheet_toolbar)
            val commentsSortingMenuBtn = commentsSheetToolbar.menu.findItem(R.id.comments_sorting).actionView
            val commentsSortingSpinner = commentsSortingMenuBtn.findViewById<Spinner>(R.id.comments_sorting_spinner)
            commentsSortingSpinner.adapter = commentsSortingOptions
            commentsSortingSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val newValue = position == 0
                    val oldValue = releaseDetails.fromMostRecent.value
                    if (newValue != oldValue) {
                        releaseDetails.fromMostRecent.value = newValue
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            commentsSortingMenuBtn.setOnClickListener {
                commentsSortingSpinner.performClick()
            }

            commentsSheetToolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.close_comments -> {
                        commentsSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                        true
                    }
                    else -> false
                }
            }

            releaseDetails.fromMostRecent.observe(this, { _ ->
                // Clear adapter if comments null, reverse list to sort from most recent
                val comments = releaseDetails.details.value?.comments

                commentsList.stopScroll()
                // Hax to scroll to the top
                commentsListAdapter.setList(null)
                if (comments != null) {
                    commentsListAdapter.setList(releaseDetails.sortCommentsIfNecessary(comments))
                }
            })

            releaseDetails.details.observe(this, { details ->

                lifecycleScope.launch(Dispatchers.IO) {
                    val isTracked = details.user?.let { trackedUser ->
                        releasesTrackerViewModel.getTrackerByUsername(trackedUser, details.releaseId.dataSource) != null
                    }

                    withContext(Dispatchers.Main) {
                        submitter.text = getString(
                            R.string.release_submitter,
                            details.user?.let { details.user } ?: run { getString(R.string.submitter_null) })

                        // Clear adapter if comments null, reverse list to sort from most recent
                        commentsListAdapter.setList(
                            if (details.comments != null) releaseDetails.sortCommentsIfNecessary(details.comments) else null
                        )
                        commentsSection.visibility = View.VISIBLE
                        commentsCount.text = if (details.comments.isNullOrEmpty()) {
                            commentsSection.isEnabled = false
                            commentsViewAll.visibility = View.GONE
                            getString(R.string.release_no_comments_title)
                        } else {
                            commentsSection.isEnabled = true
                            commentsViewAll.visibility = View.VISIBLE
                            getString(R.string.release_comments_count_title, details.comments.size)
                        }

                        markdownView.loadMarkdown(details.descriptionMarkdown)

                        releaseTrackerFragmentSharedViewModel.currentUserTracked.value = isTracked == true

                        val progressFrame = findViewById<View>(R.id.progress_frame)
                        if (progressFrame.visibility == View.VISIBLE) {
                            // Hide loading circle
                            findViewById<View>(R.id.progress_frame).visibility = View.GONE
                            findViewById<View>(R.id.release_extra_data).visibility = View.VISIBLE
                            markdownView.visibility = View.VISIBLE
                        }
                    }
                    setupTrackerButton(details)
                }
            })

            lifecycleScope.launch(Dispatchers.IO) {
                localNyaaDbViewModel.addToViewed(nyaaRelease)

                val saveBtn = findViewById<ImageButton>(R.id.save_btn)
                val active = localNyaaDbViewModel.isSaved(it)
                withContext(Dispatchers.Main) {
                    saveBtn.isActivated = active
                }
                saveBtn.setOnClickListener { view ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val active = localNyaaDbViewModel.toggleSaved(it)
                        withContext(Dispatchers.Main) {
                            view.isActivated = active
                        }
                    }
                }

                if (savedInstanceState == null) {
                    releaseDetails.requestDetails(it.getReleaseId())
                }
            }
        } ?: run {
            finish()
        }
    }

    override fun onBackPressed() {
        // Use back button also as collapse button for comments
        if (commentsSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            commentsSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(USER_LATEST_RELEASE_INTENT_OBJ, latestRelease)
        outState.putBoolean("goBackFabHidden", commentsBackToTop.isOrWillBeHidden)
        super.onSaveInstanceState(outState)
    }

    private suspend fun setupTrackerButton(details: NyaaReleaseDetails) {
        if (details.user != null) {
            val subscribedUser = releasesTrackerViewModel.getTrackerByUsername(details.user, details.releaseId.dataSource)
            latestRelease?.let {
            } ?: run {
                latestRelease = try {
                    NyaaPageProvider.getPageItems(dataSource = details.releaseId.dataSource,
                        AppUtils.getUseProxy(this@NyaaReleaseActivity),
                        pageIndex = 0, user = details.user)?.items?.getOrNull(0)
                } catch (e: Exception) {
                    null
                }
            }
            val latestTimestamp = latestRelease?.let {
                it.timestamp
            } ?: run {
                subscribedUser?.latestReleaseTimestamp
            }
            latestTimestamp?.let {
                val isTrackedAtBeginning = subscribedUser != null
                withContext(Dispatchers.Main) {
                    setButtonTracked(isTrackedAtBeginning)
                    manageTrackerBtn.isEnabled = true
                    manageTrackerBtn.visibility = View.VISIBLE
                    manageTrackerBtn.setOnClickListener {
                        val bottomSheet = ReleaseTrackerBottomFragment.newInstance(
                            details.releaseId.dataSource, details.user, latestTimestamp)
                        bottomSheet.show(supportFragmentManager, null)
                    }
                }
            }
        }
    }

    private fun openReleaseLink(release: NyaaReleasePreview) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(AppUtils.getReleasePageUrl(release.getReleaseId(), false))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun setButtonTracked(tracked: Boolean) {
        manageTrackerBtn.text = manageTrackerBtn.context.getString(
            if (tracked) R.string.manage_trackers_title else R.string.add_tracker_title
        )
    }

    companion object {
        const val RELEASE_PREVIEW_INTENT_OBJ = "nyaaReleasePreview"
        const val USER_LATEST_RELEASE_INTENT_OBJ = "userLatestRelease"

        fun startNyaaReleaseActivity(release: NyaaReleasePreview, activity: Activity) {
            val intent = Intent(activity, NyaaReleaseActivity::class.java).putExtra(
                RELEASE_PREVIEW_INTENT_OBJ, release)
            activity.startActivity(intent)
        }
    }
}