package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NyaaSearchHistoryViewModel(application: Application): AndroidViewModel(application) {
    private val dao = NyaaDb(application.applicationContext).nyaaSearchHistoryDao()

    val searchHistoryFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterSearchHistory = dao.getAll()

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
    }

    fun insert(item: NyaaSearchHistoryItem) {
        val formattedItem = NyaaSearchHistoryItem(item.searchQuery.trim(), item.searchTimestamp)
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(formattedItem)
        }
    }
}