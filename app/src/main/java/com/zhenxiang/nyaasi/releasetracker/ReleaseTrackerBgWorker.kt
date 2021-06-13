package com.zhenxiang.nyaasi.releasetracker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zhenxiang.nyaasi.BuildConfig
import com.zhenxiang.nyaasi.NyaaApplication.Companion.RELEASE_TRACKER_CHANNEL_ID
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.api.NyaaPageProvider
import com.zhenxiang.nyaasi.db.NyaaDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReleaseTrackerBgWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val TAG = javaClass.name
    private val RELEASE_TRACKER_NOTIF_ID = 1069

    private val subscribedUsersDao = NyaaDb(appContext).subscribedUsersDao()

    override suspend fun doWork(): Result {
        val usersWithNewReleases = mutableListOf<SubscribedUser>()
        withContext(Dispatchers.IO) {
            subscribedUsersDao.getAll().forEach {
                val newReleasesOfUser = getNewReleasesFromUser(it)
                if (newReleasesOfUser.isNotEmpty()) {
                    usersWithNewReleases.add(it)
                }
            }
        }
        withContext(Dispatchers.Main) {
            var notifcationContent: String? = if (usersWithNewReleases.isEmpty()) {
                if (BuildConfig.DEBUG) "[DEBUG] No new releases" else null
            } else {
                var usersListString = ""
                usersWithNewReleases.forEachIndexed { index, subscribedUser ->
                    if (index == 0) {
                        usersListString += subscribedUser.username
                    } else if (index == usersWithNewReleases.size - 1) {
                        usersListString += applicationContext.getString(R.string.and_conjunction_word, subscribedUser.username)
                    } else {
                        usersListString += applicationContext.getString(R.string.comma_word, subscribedUser.username)
                    }
                }
                applicationContext.getString(R.string.release_tracker_new_releases, usersListString)
            }
            notifcationContent?.let {
                val notificationBuilder = NotificationCompat.Builder(applicationContext, RELEASE_TRACKER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_magnet)
                    .setContentTitle("New releases")
                    .setContentText(it)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(applicationContext)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(RELEASE_TRACKER_NOTIF_ID, notificationBuilder.build())
                }
            }
        }
        return Result.success()
    }

    private suspend fun getNewReleasesFromUser(user: SubscribedUser): MutableList<Int> {
        val newReleases = mutableListOf<Int>()
        var pageIndex = 0
        while(true) {
            // Parse pages until we hit null or empty page
            val releases = NyaaPageProvider.getPageItems(pageIndex, user = user.username)
            if (releases == null || releases.isEmpty()) {
                return newReleases
            } else {
                releases.forEach {
                    // If release timestamp is smaller or equal than lastReleaseTimestamp
                    // we've hit a release than the last one saved in tracker,
                    // so let's exit and call it a day
                    if (user.lastReleaseTimestamp >= it.date.time) {
                        return newReleases
                    } else {
                        newReleases.add(it.id)
                    }
                }
            }
            pageIndex ++
        }
    }

    companion object {
        const val WORK_NAME = "releaseTrackerWork"
    }
}