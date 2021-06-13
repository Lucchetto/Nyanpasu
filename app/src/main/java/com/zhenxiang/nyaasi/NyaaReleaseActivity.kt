package com.zhenxiang.nyaasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.tiagohm.markdownview.MarkdownView
import br.tiagohm.markdownview.css.styles.Github
import com.zhenxiang.nyaasi.api.NyaaPageProvider
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import com.zhenxiang.nyaasi.db.LocalNyaaDbViewModel
import com.zhenxiang.nyaasi.db.NyaaReleaseDetails
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerViewModel
import com.zhenxiang.nyaasi.releasetracker.SubscribedUser
import com.zhenxiang.nyaasi.view.ReleaseDataItemView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.DateFormat

class NyaaReleaseActivity : AppCompatActivity() {

    private val TAG = javaClass.name

    private lateinit var markdownView: MarkdownView
    private lateinit var submitter: TextView
    private lateinit var addToTrackerBtn: Button

    private lateinit var releasesTrackerViewModel: ReleaseTrackerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nyaa_release)

        val scrollRoot = findViewById<NestedScrollView>(R.id.scroll_root)
        scrollRoot.isNestedScrollingEnabled = false

        markdownView = findViewById(R.id.release_details_markdown)
        markdownView.addStyleSheet(Github())
        markdownView.clipToOutline = true
        submitter = findViewById(R.id.submitter)

        val nyaaRelease = intent.getSerializableExtra(RELEASE_INTENT_OBJ) as NyaaReleasePreview?

        val localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        releasesTrackerViewModel = ViewModelProvider(this).get(ReleaseTrackerViewModel::class.java)

        nyaaRelease?.let {
            addToTrackerBtn = findViewById(R.id.add_to_tracker)

            val releaseTitle = findViewById<TextView>(R.id.release_title)
            releaseTitle.text = it.name

            val idView = findViewById<TextView>(R.id.release_id)
            idView.text = "ID: ${it.id}"

            val magnetBtn = findViewById<View>(R.id.magnet_btn)
            magnetBtn.setOnClickListener { _ ->
                AppUtils.openMagnetLink(this, it, scrollRoot)
            }

            val saveBtn = findViewById<ImageButton>(R.id.save_btn)
            lifecycleScope.launch(Dispatchers.IO) {
                val active = localNyaaDbViewModel.isSaved(it)
                withContext(Dispatchers.Main) {
                    saveBtn.isActivated = active
                }
            }

            saveBtn.setOnClickListener { view ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val active = localNyaaDbViewModel.toggleSaved(it)
                    withContext(Dispatchers.Main) {
                        view.isActivated = active
                    }
                }
            }

            val category = findViewById<TextView>(R.id.category)
            category.text = getString(R.string.release_category, getString(it.category.stringResId))

            val date = findViewById<TextView>(R.id.date)
            date.text = getString(R.string.release_date,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(it.date))

            val seeders = findViewById<ReleaseDataItemView>(R.id.seeders)
            seeders.setValue(it.seeders.toString())

            val leechers = findViewById<ReleaseDataItemView>(R.id.leechers)
            leechers.setValue(it.leechers.toString())

            val completed = findViewById<ReleaseDataItemView>(R.id.completed)
            completed.setValue(it.completed.toString())

            val releaseSizeView = findViewById<ReleaseDataItemView>(R.id.release_size)
            releaseSizeView.setValue(it.releaseSize)

            lifecycleScope.launch(Dispatchers.IO) {
                localNyaaDbViewModel.getDetailsById(it.id)?.let { details ->
                    withContext(Dispatchers.Main) {
                        setDetails(details)
                    }
                }

                try {
                    val doc: Document = Jsoup.connect("https://nyaa.si/view/${it.id}").get()
                    doc.outputSettings().prettyPrint(false)

                    val userName = doc.selectFirst("div.col-md-1:matches(Submitter:)").parent().select("a[href~=^(.*?)\\/user\\/(.+)\$]").text()
                    val hash = doc.selectFirst("div.col-md-1:matches(Info hash:)").parent().select("kbd:matches(^(\\w{40})\$)").text()
                    val descriptionMarkdown = doc.getElementById("torrent-description").html()

                    val details = NyaaReleaseDetails(nyaaRelease.id, if (userName.isNullOrEmpty()) null else userName, hash, descriptionMarkdown)

                    localNyaaDbViewModel.addToViewed(nyaaRelease)
                    localNyaaDbViewModel.addDetails(details)

                    withContext(Dispatchers.Main) {
                        setDetails(details)
                    }

                    setupTrackerButton(details)
                } catch(e: Exception) {
                    Log.w(TAG, e)
                }
            }
        } ?: run {
            finish()
        }
    }

    private fun setDetails(details: NyaaReleaseDetails) {
        submitter.text = getString(R.string.release_submitter,
            details.user?.let { details.user } ?: run { getString(R.string.submitter_null) })
        markdownView.loadMarkdown(details.descriptionMarkdown)

        val progressFrame = findViewById<View>(R.id.progress_frame)
        if (progressFrame.visibility == View.VISIBLE) {
            // Hide loading circle
            findViewById<View>(R.id.progress_frame).visibility = View.GONE
            findViewById<View>(R.id.release_extra_data).visibility = View.VISIBLE
            markdownView.visibility = View.VISIBLE
        }
    }

    private suspend fun setupTrackerButton(details: NyaaReleaseDetails) {
        if (details.user != null) {
            val releasesOfUser = NyaaPageProvider.getPageItems(0, user = details.user)
            releasesOfUser?.getOrNull(0)?.let { latestRelease ->
                withContext(Dispatchers.Main) {
                    addToTrackerBtn.setOnClickListener {
                        lifecycleScope.launch(Dispatchers.IO) {
                            releasesTrackerViewModel.addUserToTracker(
                                SubscribedUser(details.user, latestRelease.date.time))
                        }
                    }
                    addToTrackerBtn.isEnabled = true
                }
            }
        }
    }

    companion object {
        const val RELEASE_INTENT_OBJ = "nyaaRelease"

        fun startNyaaReleaseActivity(release: NyaaReleasePreview, activity: Activity) {
            val intent = Intent(activity, NyaaReleaseActivity::class.java).putExtra(RELEASE_INTENT_OBJ, release)
            activity.startActivity(intent)
        }
    }
}