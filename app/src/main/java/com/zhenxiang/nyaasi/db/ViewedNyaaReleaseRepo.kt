package com.zhenxiang.nyaasi.db

import android.app.Application

class ViewedNyaaReleaseRepo(application: Application) {

    val dao = NyaaDb(application.applicationContext).viewedNyaaReleasesDao()
}