package com.zhenxiang.nyaasi.releasetracker

import android.app.Application
import com.zhenxiang.nyaasi.db.NyaaDb

class ReleaseTrackerRepo(application: Application) {

    val subscribedUsersDao = NyaaDb(application.applicationContext).subscribedUsersDao()
}