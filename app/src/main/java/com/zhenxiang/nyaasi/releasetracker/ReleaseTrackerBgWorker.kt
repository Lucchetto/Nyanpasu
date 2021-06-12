package com.zhenxiang.nyaasi.releasetracker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zhenxiang.nyaasi.BuildConfig
import com.zhenxiang.nyaasi.NyaaApplication.Companion.RELEASE_TRACKER_CHANNEL_ID
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.db.NyaaDb

class ReleaseTrackerBgWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    private val TAG = javaClass.name
    private val RELEASE_TRACKER_NOTIF_ID = 1069

    private val subscribedUsersDao = NyaaDb(appContext).subscribedUsersDao()

    override fun doWork(): Result {
        if (BuildConfig.DEBUG) {
            var noficationBuilder = NotificationCompat.Builder(applicationContext, RELEASE_TRACKER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_magnet)
                .setContentTitle("New releases")
                .setContentText("Users to fetch ${subscribedUsersDao.getAll().size}")
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(applicationContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(RELEASE_TRACKER_NOTIF_ID, noficationBuilder.build())
            }
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "releaseTrackerWork"
    }
}