package com.zhenxiang.nyaa.release

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.api.ReleaseComment
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaDb
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Simple viewmodel to save release details for NyaaReleaseActivity across creations
class ReleaseDetailsHolderViewModel(application: Application): AndroidViewModel(application) {
    private val detailsDao = NyaaDb(application.applicationContext).nyaaReleasesDetailsDao()

    val details = MutableLiveData<NyaaReleaseDetails>()
    val fromMostRecent = MutableLiveData(true)

    fun requestDetails(id: ReleaseId) {
        viewModelScope.launch(Dispatchers.IO) {
            // First we try to read details from local database
            detailsDao.getItemById(id.number, id.dataSource)?.let {
                details.postValue(it)
            }

            // Then parse updated data from server
            NyaaPageProvider.getReleaseDetails(id)?.let {
                details.postValue(it)
                // Sync local database's data with new data from server
                detailsDao.insert(it)
            }
        }
    }

    fun sortCommentsIfNecessary(comments: List<ReleaseComment>): List<ReleaseComment> {
        // Assume we got data from NyaaPageProvider which gives by default from least recent comment
        return if (fromMostRecent.value == true) {
            comments.reversed()
        } else {
            comments
        }
    }
}