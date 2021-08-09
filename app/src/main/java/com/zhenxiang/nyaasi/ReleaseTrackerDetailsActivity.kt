package com.zhenxiang.nyaasi

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerRepo
import com.zhenxiang.nyaasi.releasetracker.SubscribedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class ReleaseTrackerDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release_tracker_details)

        val tracker = intent.getSerializableExtra(RELEASE_TRACKER_INTENT_OBJ) as SubscribedTracker?

        tracker?.let { _ ->
            val title = findViewById<TextView>(R.id.tracker_title)
            val category = findViewById<TextView>(R.id.tracker_category)
            val username = findViewById<TextView>(R.id.tracker_username)
            val latestRelease = findViewById<TextView>(R.id.latest_release_date)
            val trackerCreatedDate = findViewById<TextView>(R.id.tracker_created_date)
            val deleteBtn = findViewById<TextView>(R.id.delete_tracker_btn)

            category.text = getString(R.string.release_category,
                getString(tracker.category.stringResId)
            )
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
                    ReleaseTrackerRepo(application).dao.deleteById(tracker.id)
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