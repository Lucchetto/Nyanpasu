package com.zhenxiang.nyaasi.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NyaaSearchViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val searchResultsLiveData = MutableLiveData(repository.items)
    private var busy = false

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
        viewModelScope.launch {
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

    fun isBottomReached() = this.repository.bottomReached
}