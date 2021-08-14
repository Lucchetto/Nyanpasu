package com.zhenxiang.nyaa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.styles.Github
import com.revengeos.revengeui.utils.NavigationModeUtils
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
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

class NyaaReleaseActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    private lateinit var scrollRoot: NestedScrollView
    private lateinit var markdownView: MarkdownView
    private lateinit var submitter: TextView
    private lateinit var manageTrackerBtn: Button

    private lateinit var releasesTrackerViewModel: ReleaseTrackerViewModel
    private lateinit var releaseTrackerFragmentSharedViewModel: ReleaseTrackerFragmentSharedViewModel
    private var latestRelease: NyaaReleasePreview? = null

    private var queuedDownload: ReleaseId? = null
    private val storagePermissionGuard = createPermissionRequestLauncher { granted ->
        queuedDownload?.let {
            if (granted) {
                AppUtils.enqueueDownload(it, scrollRoot)
            } else {
                AppUtils.storagePermissionForDownloadDenied(scrollRoot)
            }
            queuedDownload = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        scrollRoot = findViewById(R.id.scroll_root)
        scrollRoot.isNestedScrollingEnabled = false
        scrollRoot.applyInsetter {
            type(navigationBars = !NavigationModeUtils.isFullGestures(scrollRoot.context), statusBars = true) {
                margin()
            }
        }

        markdownView = findViewById(R.id.release_details_markdown)
        markdownView.addStyleSheet(Github())
        markdownView.clipToOutline = true
        submitter = findViewById(R.id.submitter)

        val nyaaRelease = intent.getSerializableExtra(RELEASE_PREVIEW_INTENT_OBJ) as NyaaReleasePreview?
        latestRelease = savedInstanceState?.getSerializable(USER_LATEST_RELEASE_INTENT_OBJ) as NyaaReleasePreview?

        val localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)
        releaseTrackerFragmentSharedViewModel = ViewModelProvider(this).get(ReleaseTrackerFragmentSharedViewModel::class.java)

        nyaaRelease?.let {
            manageTrackerBtn = findViewById(R.id.add_to_tracker)
            releaseTrackerFragmentSharedViewModel.currentUserTracked.observe(this) {
                setButtonTracked(it)
            }

            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name

            val idView = findViewById<TextView>(R.id.release_id)
            idView.text = getString(R.string.release_id_content, it.number.toString(), it.dataSourceSpecs.source.url)

            val magnetBtn = findViewById<View>(R.id.magnet_btn)
            magnetBtn.setOnClickListener { _ ->
                AppUtils.openMagnetLink(it, scrollRoot)
            }

            val downloadBtn = findViewById<View>(R.id.download_btn)
            downloadBtn.setOnClickListener { _ ->
                val newDownload = it.getReleaseId()
                AppUtils.guardDownloadPermission(this, storagePermissionGuard, {
                    AppUtils.enqueueDownload(newDownload, scrollRoot)
                }, {
                    queuedDownload = newDownload
                })
            }

            val category = findViewById<TextView>(R.id.category)
            category.text = AppUtils.getReleaseCategoryString(this, it.dataSourceSpecs.category)

            val date = findViewById<TextView>(R.id.date)
            date.text = getString(R.string.release_date,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(it.timestamp * 1000)))

            val seeders = findViewById<ReleaseDataItemView>(R.id.seeders)
            seeders.setValue(it.seeders.toString())

            val leechers = findViewById<ReleaseDataItemView>(R.id.leechers)
            leechers.setValue(it.leechers.toString())

            val completed = findViewById<ReleaseDataItemView>(R.id.completed)
            completed.setValue(it.completed.toString())

            val releaseSizeView = findViewById<ReleaseDataItemView>(R.id.release_size)
            releaseSizeView.setValue(it.releaseSize)

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

                val releaseId = it.getReleaseId()
                val localReleaseDetails = localNyaaDbViewModel.getDetailsById(releaseId)
                localReleaseDetails?.let { details ->
                    setDetails(details)
                }

                if (localReleaseDetails == null && savedInstanceState == null) {
                    NyaaPageProvider.getReleaseDetails(releaseId)?.let { details ->
                        localNyaaDbViewModel.addDetails(details)

                        setDetails(details)
                    }
                }
            }
        } ?: run {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(USER_LATEST_RELEASE_INTENT_OBJ, latestRelease)
        super.onSaveInstanceState(outState)
    }

    private suspend fun setDetails(details: NyaaReleaseDetails) {
        val isTracked = details.user?.let {
                releasesTrackerViewModel.getTrackerByUsername(it) != null
        }

        withContext(Dispatchers.Main) {
            submitter.text = getString(R.string.release_submitter,
                details.user?.let { details.user } ?: run { getString(R.string.submitter_null) })
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

    private suspend fun setupTrackerButton(details: NyaaReleaseDetails) {
        if (details.user != null) {
            val subscribedUser = releasesTrackerViewModel.getTrackerByUsername(details.user)
            latestRelease?.let {
            } ?: run {
                latestRelease = NyaaPageProvider.getPageItems(dataSource = details.releaseId.dataSource,
                    pageIndex = 0, user = details.user)?.items?.getOrNull(0)
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

    private fun setButtonTracked(tracked: Boolean) {
        manageTrackerBtn.text = manageTrackerBtn.context.getString(
            if (tracked) R.string.manage_trackers_title else R.string.add_tracker_title)
    }

    companion object {
        const val RELEASE_PREVIEW_INTENT_OBJ = "nyaaReleasePreview"
        const val USER_LATEST_RELEASE_INTENT_OBJ = "userLatestRelease"

        fun startNyaaReleaseActivity(release: NyaaReleasePreview, activity: Activity) {
            val intent = Intent(activity, NyaaReleaseActivity::class.java).putExtra(RELEASE_PREVIEW_INTENT_OBJ, release)
            activity.startActivity(intent)
        }
    }
}