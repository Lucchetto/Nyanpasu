package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)

    private val viewedReleasesRepo = ViewedNyaaReleaseRepo(application)
    private val viewedItems = mutableListOf<NyaaReleasePreview>()
    val viewedReleases = MutableLiveData<MutableList<NyaaReleasePreview>>()

    init {
        getAll()
    }

    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            viewedItems.clear()
            viewedItems.addAll(viewedReleasesRepo.dao.getAllWithDetails().map { item -> item.details })
            withContext(Dispatchers.Main) {
                viewedReleases.value = viewedItems
            }
        }
    }

    fun searchViewedRelease(query: String?) {
        query?.let {
             viewedItems.filter { item -> item.name.contains(query, true) }.let {
                 viewedReleases.value = it.toMutableList()
            }
        } ?: run {
            viewedReleases.value = viewedItems
        }
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