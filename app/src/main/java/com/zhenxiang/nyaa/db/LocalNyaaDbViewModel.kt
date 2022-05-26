package com.zhenxiang.nyaa.db

import android.app.Application
import androidx.lifecycle.*
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)

    val viewedReleasesSearchFilter = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    ).apply {
        // Required to emit value for searchHistory on start
        tryEmit(null)
    }

    // Filtered list exposed for usage
    val viewedReleases = combine(nyaaLocalRepo.viewedDao.getAllWithDetails(), viewedReleasesSearchFilter) { releases, query ->
        if (query.isNullOrBlank()) {
            releases
        } else {
            releases.filter { it.details.name.contains(query, true) }
        }.map { it.details }
    }

    val savedReleasesSearchFilter = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    ).apply {
        // Required to emit value for searchHistory on start
        tryEmit(null)
    }

    // Filtered list exposed for usage
    val savedReleases = combine(nyaaLocalRepo.savedDao.getAllWithDetails(), savedReleasesSearchFilter) { releases, query ->
        if (query.isNullOrBlank()) {
            releases
        } else {
            releases.filter { it.details.name.contains(query, true) }
        }.map { it.details }
    }

    fun removeViewed(release: NyaaReleasePreview) {
        viewModelScope.launch(Dispatchers.IO) {
            nyaaLocalRepo.viewedDao.deleteById(release.number, release.dataSourceSpecs.source)
            // Catch any SQLITE_CONSTRAINT_TRIGGER caused by constraints of viewed table foreign key
            try {
                nyaaLocalRepo.previewsDao.delete(release.number, release.dataSourceSpecs.source)
            } catch (e: Exception) {}
        }
    }

    fun isSaved(release: NyaaReleasePreview): Boolean {
        return nyaaLocalRepo.savedDao.getItemById(release.number, release.dataSourceSpecs.source) != null
    }

    fun toggleSaved(release: NyaaReleasePreview): Boolean {
        nyaaLocalRepo.savedDao.getItemById(release.number, release.dataSourceSpecs.source)?.let {
            nyaaLocalRepo.savedDao.delete(it)
            // Catch any SQLITE_CONSTRAINT_TRIGGER caused by constraints of viewed table foreign key
            try {
                nyaaLocalRepo.previewsDao.delete(it.releaseId.number, it.releaseId.dataSource)
            } catch (e: Exception) {}
            return false
        } ?: run {
            nyaaLocalRepo.savedDao.insert(SavedNyaaRelease(release.getReleaseId(), System.currentTimeMillis()))
            return true
        }
    }
}
