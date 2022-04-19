package com.zhenxiang.nyaa.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.api.NyaaPageProvider
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.model.SearchSpecsModel
import com.zhenxiang.nyaa.model.SearchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * A [AndroidViewModel] which provides search results for releases
 */
open class BrowseViewModel(application: Application): AndroidViewModel(application) {

    val searchSpecs = SearchSpecsModel()

    val resultsFlow = MutableStateFlow(emptyList<NyaaReleasePreview>())
    val searchStatusFlow = MutableStateFlow<SearchStatus>(SearchStatus.Ready)
    var hasScrolled: Boolean = false

    private var loadResultsInternalJob: Job? = null

    /**
     * Clear all results and load results for current [SearchSpecsModel]
     */
    fun loadResults() {
        // Stop previous job first
        loadResultsInternalJob?.let {
            if (it.isActive) it.cancel()
        }

        loadResultsInternalJob = viewModelScope.launch(Dispatchers.Default) {
            searchSpecs.pageIndex = 1
            hasScrolled = false
            resultsFlow.emit(emptyList())
            loadResultsInternal()
        }
    }

    /**
     * Load next page results for current [SearchSpecsModel]
     */
    fun nextPage() {
        // Don't load next page if status isn't ready or a job is still running
        if (searchStatusFlow.value != SearchStatus.Ready || loadResultsInternalJob?.isActive == true) {
            return
        }
        searchSpecs.pageIndex++

        loadResultsInternalJob = viewModelScope.launch(Dispatchers.Default) {
            loadResultsInternal()
        }
    }

    private suspend fun loadResultsInternal() {
        try {
            searchStatusFlow.emit(SearchStatus.Loading)
            val results = NyaaPageProvider.runSearch(searchSpecs)
            resultsFlow.emit(resultsFlow.value + results.items)
            searchStatusFlow.emit(if (results.bottomReached) SearchStatus.End else SearchStatus.Ready)
        } catch (e: Exception) {
            searchStatusFlow.emit(SearchStatus.Error)
        }
    }
}
