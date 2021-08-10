package com.zhenxiang.nyaa.releasetracker

import android.app.Application
import com.zhenxiang.nyaa.db.NyaaDb

class ReleaseTrackerRepo(application: Application) {

    val dao = NyaaDb(application.applicationContext).subscribedTrackersDao()
}