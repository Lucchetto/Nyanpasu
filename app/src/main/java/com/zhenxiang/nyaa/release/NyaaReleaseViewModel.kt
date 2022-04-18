package com.zhenxiang.nyaa.release

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaDb
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.ext.latestValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Simple viewmodel to save release details for NyaaReleaseActivity across creations
class NyaaReleaseViewModel(
    application: Application,
    private val state: SavedStateHandle,
): AndroidViewModel(application) {

    private val detailsDao = NyaaDb.invoke(application).nyaaReleasesDetailsDao()

    val releaseDetailsFlow = MutableSharedFlow<NyaaReleaseDetails>(replay = 1)
    val fromMostRecentFlow = MutableStateFlow(true)
    val commentsFlow = releaseDetailsFlow.combine(fromMostRecentFlow) {details, fromMostRecent ->
        details.comments?.let {
            if (fromMostRecent) it.reversed() else it
        }
    }

    var releasePreview: NyaaReleasePreview? = null
    set(value) {
        // Do nothing if preview data is the same
        if (value == field) {
            return
        }
        field = value
        onReleasePreviewDataChanged()
    }

    private fun onReleasePreviewDataChanged() {
        releasePreview?.let {
            viewModelScope.launch(Dispatchers.Default) {
                // Try to load details from db cache first if previous releaseDetails' id wasn't equal to newReleaseId
                val newReleaseId = it.getReleaseId()
                if (releaseDetailsFlow.latestValue?.releaseId != newReleaseId) {
                    detailsDao.getItemById(newReleaseId.number, newReleaseId.dataSource)?.let { cachedDetails ->
                        releaseDetailsFlow.emit(cachedDetails)
                    }
                }

                loadReleaseDetailsFromNetwork(newReleaseId)?.let {
                    releaseDetailsFlow.emit(it)
                    detailsDao.insert(it)
                }
            }
        }
    }

    private suspend fun loadReleaseDetailsFromNetwork(releaseId: ReleaseId): NyaaReleaseDetails? =
        NyaaPageProvider.getReleaseDetails(releaseId)
}