package com.zhenxiang.nyaa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.*
import com.zhenxiang.nyaa.releasetracker.ReleaseTrackerBgWorker
import com.zhenxiang.nyaa.util.UIModeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.google.android.gms.ads.initialization.InitializationStatus

import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

import com.google.android.gms.ads.MobileAds




class NyaaApplication: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        UIModeUtils.updateUIMode(applicationContext)
        super.onCreate()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            setupWorks()
            setupAdMob()
            setupNotificationChannels()
        }
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = applicationContext.getString(R.string.releases_tracker_notif_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(RELEASE_TRACKER_CHANNEL_ID, name, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

    private fun setupAdMob() {
        MobileAds.initialize(
            this
        ) { }

    }

    private fun setupWorks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val releaseTracker =
            PeriodicWorkRequestBuilder<ReleaseTrackerBgWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this@NyaaApplication)
            .enqueueUniquePeriodicWork(ReleaseTrackerBgWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, releaseTracker)
    }

    companion object {
        const val RELEASE_TRACKER_CHANNEL_ID = "releaseTracker"
    }
}