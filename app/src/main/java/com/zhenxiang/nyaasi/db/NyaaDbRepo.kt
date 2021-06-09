package com.zhenxiang.nyaasi.db

import android.app.Application

class NyaaDbRepo(application: Application) {

    val dao = NyaaDb(application.applicationContext).nyaaReleasesDao()
}