package com.zhenxiang.nyaasi.db

import android.app.Application

class NyaaDbRepo(application: Application) {

    val previewsDao = NyaaDb(application.applicationContext).nyaaReleasesPreviewDao()
    val detailsDao = NyaaDb(application.applicationContext).nyaaReleasesDetailsDao()
}