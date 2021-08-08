package com.zhenxiang.nyaasi.releasetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class ReleaseTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReleaseTrackerRepo(application)

    val subscribedTrackerSearch = MutableLiveData<String>()
    private val preFilterSubscribedTrackers = repo.dao.getAllTrackersLive()
    val subscribedTrackers = Transformations.switchMap(subscribedTrackerSearch) { query ->
        if (query.isNullOrBlank()) {
            preFilterSubscribedTrackers
        } else {
            Transformations.map(preFilterSubscribedTrackers) {
                it.filter { item -> item.username?.contains(query, true) == true || item.searchQuery?.contains(query, true) == true }
            }
        }
    }

    init {
        subscribedTrackerSearch.value = null
    }

    fun addReleaseTracker(tracker: SubscribedTracker) {
        repo.dao.insert(tracker)
    }

    fun getTrackedByUsername(username: String): SubscribedTracker? {
        return repo.dao.getByUsername(username)
    }

    fun getTrackedByUsernameAndQuery(username: String?, query: String): SubscribedTracker? {
        return if (username.isNullOrBlank()) {
            repo.dao.getByQueryWithNullUsername(query)
        } else {
            repo.dao.getByUsernameAndQuery(username, query)
        }
    }

    fun deleteTrackedUser(username: String) {
        repo.dao.deleteByUsername(username)
    }
}