package com.zhenxiang.nyaa.release

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.*
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.ext.collectInScope
import com.zhenxiang.nyaa.ext.getNonNull
import com.zhenxiang.nyaa.ext.latestValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Simple [ViewModel] to save release details for [NyaaReleaseActivity] across creations
 */
class NyaaReleaseViewModel(
    application: Application,
    private val state: SavedStateHandle,
): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)
    private val detailsDao = NyaaDb.invoke(application).nyaaReleasesDetailsDao()

    private val _releasePreviewFlow = MutableSharedFlow<NyaaReleasePreview>(replay = 1)
    val releasePreviewFlow: Flow<NyaaReleasePreview> = _releasePreviewFlow

    val releaseDetailsFlow = MutableSharedFlow<NyaaReleaseDetails>(replay = 1)
    val fromMostRecentFlow = MutableStateFlow(state.getNonNull(FROM_MOST_RECENT_KEY, true))
    val commentsFlow = releaseDetailsFlow.combine(fromMostRecentFlow) {details, fromMostRecent ->
        details.comments?.let {
            if (fromMostRecent) it.reversed() else it
        }
    }

    init {
        state.get<NyaaReleasePreview>(RELEASE_PREVIEW_KEY)?.let {
            setReleasePreview(it)
        }

        _releasePreviewFlow.collectInScope(viewModelScope) {
            state.set(RELEASE_PREVIEW_KEY, it)
        }
        fromMostRecentFlow.collectInScope(viewModelScope) {
            state.set(FROM_MOST_RECENT_KEY, it)
        }
    }

    fun setReleasePreview(releasePreview: NyaaReleasePreview) {
        // Do nothing if preview data is the same
        if (releasePreview == _releasePreviewFlow.latestValue) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            addToViewed(releasePreview)
            _releasePreviewFlow.emit(releasePreview)
            loadReleaseDetails(releasePreview)
        }
    }

    private fun loadReleaseDetails(releasePreview: NyaaReleasePreview) {
        viewModelScope.launch(Dispatchers.Default) {
            // Try to load details from db cache first if previous releaseDetails' id wasn't equal to newReleaseId
            val newReleaseId = releasePreview.getReleaseId()
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

    private suspend fun addToViewed(release: NyaaReleasePreview) {
        nyaaLocalRepo.previewsDao.upsert(release)
        nyaaLocalRepo.viewedDao.insert(ViewedNyaaRelease(release.getReleaseId(), System.currentTimeMillis()))
        val toDelete = nyaaLocalRepo.viewedDao.getExcessiveRecentsIds()
        if (toDelete.isNotEmpty()) {
            nyaaLocalRepo.viewedDao.deleteByIdList(toDelete)
            // Catch any SQLITE_CONSTRAINT_TRIGGER caused by constraints of saved table foreign key
            try {
                nyaaLocalRepo.previewsDao.deleteByIdList(toDelete)
            } catch (e: Exception) {}
        }
    }

    private suspend fun loadReleaseDetailsFromNetwork(releaseId: ReleaseId): NyaaReleaseDetails? =
        NyaaPageProvider.getReleaseDetails(releaseId)

    companion object {
        private const val FROM_MOST_RECENT_KEY = "from_most_recent"
        private const val RELEASE_PREVIEW_KEY = "release_preview"
    }
}