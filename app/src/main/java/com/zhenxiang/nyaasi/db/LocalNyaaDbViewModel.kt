package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.MutableLiveData




class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)

    private val viewedReleasesRepo = ViewedNyaaReleaseRepo(application)

    val searchFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterViewedReleases = Transformations.map(viewedReleasesRepo.dao.getAllWithDetails()) {
        it.map { item -> item.details }
    }
    // Filtered list exposed for usage
    val viewedReleases = Transformations.switchMap(searchFilter) { query ->
        if (query.isNullOrEmpty()) {
            preFilterViewedReleases
        } else {
            Transformations.map(preFilterViewedReleases) { list ->
                list.filter { item -> item.name.contains(query, true) }
            }
        }
    }

    init {
        // Required to emit value for viewedReleases on start
        searchFilter.value = null
    }

    suspend fun getDetailsById(id: Int): NyaaReleaseDetails? {
        return withContext(Dispatchers.IO) {
            nyaaLocalRepo.detailsDao.getById(id)
        }
    }

    fun addToViewed(release: NyaaReleasePreview) {
        nyaaLocalRepo.previewsDao.insert(release)
        viewedReleasesRepo.dao.insert(ViewedNyaaRelease(release.id, System.currentTimeMillis()))
    }

    fun addDetails(details: NyaaReleaseDetails) {
        nyaaLocalRepo.detailsDao.insert(details)
    }
}