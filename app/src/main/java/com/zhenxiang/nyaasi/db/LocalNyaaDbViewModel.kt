package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalNyaaDbViewModel(application: Application): ViewModel() {

    private val detailsRepo = NyaaDbRepo(application)

    private val viewedReleasesRepo = ViewedNyaaReleaseRepo(application)
    private val viewedItems = mutableListOf<NyaaRelease>()
    private val viewedReleases = MutableLiveData(viewedItems)

    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            viewedItems.clear()
            viewedItems.addAll(viewedReleasesRepo.dao.getAllWithDetails().map { item -> item.details })
            viewedReleases.value = viewedItems
        }
    }

    fun addToViewed(release: NyaaRelease) {
        viewModelScope.launch(Dispatchers.IO) {
            detailsRepo.dao.insert(release)
            viewedReleasesRepo.dao.insert(ViewedNyaaRelease(release.id, System.currentTimeMillis()))
            getAll()
        }
    }
}