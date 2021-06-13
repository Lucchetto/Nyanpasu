package com.zhenxiang.nyaasi.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class NyaaSearchViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val searchResultsLiveData = MutableLiveData(repository.items)
    var busy = false
        private set

    private var job : Job? = null

    fun setSearchText(searchText: String?) {
        searchText?.let {
            repository.searchValue = if (searchText.isEmpty()) null else searchText
        } ?: run {
            repository.searchValue = null
        }
        searchResultsLiveData.value = repository.items
    }

    fun loadMore() {
        if (repository.items.size > 0 && !isBottomReached() && !busy) {
            loadSearchResults()
        }
    }

    fun loadSearchResults() {
        job = viewModelScope.launch(Dispatchers.IO) {
            busy = true
            if (repository.searchValue?.isNotEmpty() == true) {
                repository.getLinks()
            }
            withContext(Dispatchers.Main) {
                // Emit new values from repository
                searchResultsLiveData.value = repository.items
            }
            busy = false
        }
    }

    fun setCategory(category: NyaaReleaseCategory) {
        this.repository.category = category
    }

    fun isBottomReached() = this.repository.bottomReached
}