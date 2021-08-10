package com.zhenxiang.nyaa.releasetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.zhenxiang.nyaa.api.NyaaReleaseCategory

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

    fun getTrackerByUsername(username: String): SubscribedTracker? {
        return repo.dao.getByUsername(username)
    }

    suspend fun getTrackerWithSameSpecs(username: String?, query: String, category: NyaaReleaseCategory): SubscribedTracker? {
        return repo.dao.getByUsernameAndQueryAndCategory(
            if (username.isNullOrBlank()) null else username,
            query, category
        )
    }

    fun deleteTrackedUser(username: String) {
        repo.dao.deleteByUsername(username)
    }
}