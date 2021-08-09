package com.zhenxiang.nyaasi.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import kotlinx.coroutines.*

class NyaaSearchViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val searchResultsLiveData = MutableLiveData<List<NyaaReleasePreview>>()
    var busy = false
        private set
    var firstInsert: Boolean = true

    fun setSearchText(searchText: String?) {
        firstInsert = true
        repository.searchValue = searchText
    }

    fun loadMore() {
        if (repository.items.size > 0 && !endReached() && !busy) {
            loadSearchResults()
        }
    }

    fun loadSearchResults() {
        viewModelScope.launch(Dispatchers.IO) {
            busy = true
            if (repository.searchValue?.isNotEmpty() == true) {
                repository.getLinks()
            }
            withContext(Dispatchers.Main) {
                // Emit new values from repository
                searchResultsLiveData.value = repository.items.toList()
            }
            busy = false
        }
    }

    fun setCategory(category: NyaaReleaseCategory) {
        this.repository.category = category
    }

    fun setUsername(username: String?) {
        firstInsert = true
        repository.username = username
    }

    fun clearResults() {
        repository.clearRepo()
        searchResultsLiveData.value = repository.items.toList()
    }

    fun endReached() = this.repository.endReached
}