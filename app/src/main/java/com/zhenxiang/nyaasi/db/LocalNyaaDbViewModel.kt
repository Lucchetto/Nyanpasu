package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val detailsRepo = NyaaDbRepo(application)

    private val viewedReleasesRepo = ViewedNyaaReleaseRepo(application)
    private val viewedItems = mutableListOf<NyaaRelease>()
    val viewedReleases = MutableLiveData<MutableList<NyaaRelease>>()

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

    fun addToViewed(release: NyaaRelease) {
        viewModelScope.launch(Dispatchers.IO) {
            detailsRepo.dao.insert(release)
            viewedReleasesRepo.dao.insert(ViewedNyaaRelease(release.id, System.currentTimeMillis()))
        }
    }
}