package com.zhenxiang.nyaa.releasetracker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zhenxiang.nyaa.BuildConfig
import com.zhenxiang.nyaa.NyaaApplication.Companion.RELEASE_TRACKER_CHANNEL_ID
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.db.NyaaDb
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.app.PendingIntent

import com.zhenxiang.nyaa.MainActivity

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.zhenxiang.nyaa.AppUtils


class ReleaseTrackerBgWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val TAG = javaClass.name
    private val RELEASE_TRACKER_NOTIF_ID = 1072

    private val releaseTrackersDao = NyaaDb(appContext).subscribedTrackersDao()

    override suspend fun doWork(): Result {
        val trackersWithNewReleases = mutableListOf<SubscribedTracker>()
        withContext(Dispatchers.IO) {
            releaseTrackersDao.getAllTrackers().forEach {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Scanning tracker $it")
                }
                val newReleasesOfTracker = getNewReleasesFromTracker(it)
                if (newReleasesOfTracker.isNotEmpty()) {
                    Log.w(TAG, "New releases found for $it")
                    trackersWithNewReleases.add(it)
                    it.latestReleaseTimestamp = newReleasesOfTracker[0].timestamp
                    it.newReleasesCount += newReleasesOfTracker.size
                    it.hasPreviousReleases = true
                    releaseTrackersDao.update(it)
                }
            }

            val notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean(applicationContext.getString(R.string.releases_tracker_notification_key), true)

            if (notificationsEnabled && trackersWithNewReleases.isNotEmpty()) {
                val notificationTitle = applicationContext.getString(R.string.releases_tracker_notif_name)
                val notificationContent = applicationContext.resources.getQuantityString(
                    R.plurals.releases_tracker_notif_content, trackersWithNewReleases.size, trackersWithNewReleases.size)
                var notificationContentExpanded = applicationContext.getString(
                    R.string.releases_tracker_notif_expanded_content_first_line)
                trackersWithNewReleases.forEach {
                    // Make sure to break line
                    notificationContentExpanded += "\n"
                    notificationContentExpanded += when {
                        it.username != null && it.searchQuery == null -> {
                            applicationContext.getString(R.string.release_tracker_new_releases_from_user, it.username)
                        }
                        it.username == null && it.searchQuery != null -> {
                            applicationContext.getString(R.string.release_tracker_new_releases_only_search_query, it.searchQuery)
                        }
                        it.username != null && it.searchQuery != null -> {
                            applicationContext.getString(R.string.release_tracker_new_releases_from_user_with_search_query, it.searchQuery, it.username)
                        }
                        // Else should never happen
                        else -> ""
                    }
                }
                withContext(Dispatchers.Main) {
                    generateNotif(RELEASE_TRACKER_NOTIF_ID, notificationTitle, notificationContent, notificationContentExpanded)
                }
            } else {
                if (BuildConfig.DEBUG && notificationsEnabled) {
                    withContext(Dispatchers.Main) {
                        generateNotif(RELEASE_TRACKER_NOTIF_ID, applicationContext.getString(R.string.releases_tracker_notif_name), "[DEBUG] No new releases")
                    }
                }
            }
        }
        return Result.success()
    }

    private fun generateNotif(id: Int, title: String, content: String, expandedText: String? = null) {
        val activityIntent = Intent(applicationContext, MainActivity::class.java)
        // Select releases tracker tab in bottom nav
        activityIntent.putExtra(MAIN_ACTIVITY_BOTTOM_NAV_SELECTED_ID, R.id.subscribedUsers)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, RELEASE_TRACKER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_circle_notifications_24)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
            .setAutoCancel(true)

        expandedText?.let {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(it))
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, notificationBuilder.build())
        }
    }

    private suspend fun getNewReleasesFromTracker(tracker: SubscribedTracker): MutableList<NyaaReleasePreview> {
        val newReleases = mutableListOf<NyaaReleasePreview>()
        var pageIndex = 0
        while(true) {
            // Parse pages until we hit null or empty page
            val releases = try {
                NyaaPageProvider.getPageItems(tracker.dataSourceSpecs.source, AppUtils.getUseProxy(applicationContext),
                    pageIndex, category = tracker.dataSourceSpecs.category,
                    user = tracker.username, searchQuery = tracker.searchQuery)
            } catch (e: Exception) {
                null
            }
            if (releases == null || releases.items.isEmpty()) {
                return newReleases
            } else {
                releases.items.forEach {
                    // If release timestamp is smaller or equal than latestReleaseTimestamp
                    // we've hit a release than the last one saved in tracker,
                    // so let's exit and call it a day
                    if (tracker.latestReleaseTimestamp >= it.timestamp) {
                        return newReleases
                    } else {
                        newReleases.add(it)
                    }
                }
            }
            pageIndex ++
        }
    }

    companion object {
        const val WORK_NAME = "releaseTrackerWork"

        const val MAIN_ACTIVITY_BOTTOM_NAV_SELECTED_ID = "selectedItemId"
    }
}