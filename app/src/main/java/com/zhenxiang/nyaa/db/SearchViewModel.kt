package com.zhenxiang.nyaa.db

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.ext.getNonNull
import com.zhenxiang.nyaa.ui.browse.BrowseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    application: Application,
    private val state: SavedStateHandle,
): BrowseViewModel(application) {
    private val dao = NyaaDb(application.applicationContext).nyaaSearchHistoryDao()

    val searchHistoryFilter = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    ).apply {
        // Required to emit value for searchHistory on start
        tryEmit(null)
    }

    val showSuggestionsFlow = MutableStateFlow(state.getNonNull(SHOW_SUGGESTIONS_KEY, true))

    val searchHistory = combine(dao.getAllFlow(), searchHistoryFilter) { history ,query ->
        if (query.isNullOrBlank()) {
            history
        } else {
            history.filter { it.searchQuery.contains(query, true) }
        }
    }

    init {
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