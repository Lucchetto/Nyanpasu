package com.zhenxiang.nyaasi.releasetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class ReleaseTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReleaseTrackerRepo(application)

    val subscribedUserSearch = MutableLiveData<String>()
    private val preFilterSubscribedUsers = repo.subscribedUsersDao.getAllLive()
    val subscribedUsers = Transformations.switchMap(subscribedUserSearch) { query ->
        if (query.isNullOrEmpty()) {
            preFilterSubscribedUsers
        } else {
            Transformations.map(preFilterSubscribedUsers) {
                it.filter { item -> item.username.contains(query, true) }
            }
        }
    }

    init {
        subscribedUserSearch.value = null
    }

    fun addUserToTracker(user: SubscribedUser) {
        repo.subscribedUsersDao.insert(user)
    }
}