package com.zhenxiang.nyaa.db

import android.app.Application
import androidx.lifecycle.*
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)

    val viewedReleasesSearchFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterViewedReleases = Transformations.map(nyaaLocalRepo.viewedDao.getAllWithDetails()) {
        it.map { item -> item.details }
    }
    // Filtered list exposed for usage
    val viewedReleases = Transformations.switchMap(viewedReleasesSearchFilter) { query ->
        preFilterViewedReleases.searchByName(query)
    }

    val savedReleasesSearchFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterSavedReleases = Transformations.map(nyaaLocalRepo.savedDao.getAllWithDetails()) {
        it.map { item -> item.details }
    }
    // Filtered list exposed for usage
    val savedReleases = Transformations.switchMap(savedReleasesSearchFilter) { query ->
        preFilterSavedReleases.searchByName(query)
    }

    init {
        // Required to emit value for viewedReleases on start
        viewedReleasesSearchFilter.value = null
        // Required to emit value for savedReleases on start
        savedReleasesSearchFilter.value = null
    }

    fun removeViewed(release: NyaaReleasePreview) {
        viewModelScope.launch(Dispatchers.IO) {
            val releaseId = release.getReleaseId()
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

private fun LiveData<List<NyaaReleasePreview>>.searchByName(query: String?): LiveData<List<NyaaReleasePreview>> {
    return if (query.isNullOrBlank()) {
        this
    } else {
        Transformations.map(this) { list ->
            list.filter { item -> item.name.contains(query, true) }
        }
    }
}
