package com.zhenxiang.nyaa.db

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.ext.getNonNull
import com.zhenxiang.nyaa.ui.browse.BrowseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    application: Application,
    private val state: SavedStateHandle,
): BrowseViewModel(application) {
    private val dao = NyaaDb(application.applicationContext).nyaaSearchHistoryDao()

    val searchHistoryFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterSearchHistory = dao.getAllLive()

    val showSuggestionsFlow = MutableStateFlow(state.getNonNull(SHOW_SUGGESTIONS_KEY, true))

    val searchHistory = Transformations.switchMap(searchHistoryFilter) { query ->
        if (query.isNullOrBlank()) {
            preFilterSearchHistory
        } else {
            Transformations.map(preFilterSearchHistory) {
                it.filter { item -> item.searchQuery.contains(query, true) }
            }
        }
    }

    init {
        // Required to emit value for searchHistory on start
        searchHistoryFilter.value = null

        viewModelScope.launch(Dispatchers.Default) {
            showSuggestionsFlow.collect {
                state.set(SHOW_SUGGESTIONS_KEY, it)
            }
        }
    }

    fun insert(item: NyaaSearchHistoryItem) {
        val formattedItem = NyaaSearchHistoryItem(item.searchQuery.trim(), item.searchTimestamp)
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(formattedItem)
            dao.deleteExcessiveRecents()
        }
    }

    fun delete(item: NyaaSearchHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(item)
        }
    }

    companion object {
        private const val SHOW_SUGGESTIONS_KEY = "show_suggestions"
    }
}