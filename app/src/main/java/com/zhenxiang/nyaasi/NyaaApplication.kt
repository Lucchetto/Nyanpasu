package com.zhenxiang.nyaasi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.zhenxiang.nyaasi.releasetracker.ReleaseTrackerBgWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NyaaApplication: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            setupWorks()
            setupNotificationChannels()
        }
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = RELEASE_TRACKER_CHANNEL_ID
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(RELEASE_TRACKER_CHANNEL_ID, name, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

    private fun setupWorks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val releaseTracker =
            PeriodicWorkRequestBuilder<ReleaseTrackerBgWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this@NyaaApplication)
            .enqueueUniquePeriodicWork(ReleaseTrackerBgWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, releaseTracker)
    }

    companion object {
        const val RELEASE_TRACKER_CHANNEL_ID = "releaseTracker"
    }
}