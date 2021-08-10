package com.zhenxiang.nyaasi.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import kotlinx.coroutines.*

class NyaaApiViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val resultsLiveData = MutableLiveData<List<NyaaReleasePreview>>()
    var busy = false
        private set
    var firstInsert: Boolean = true

    fun setSearchText(searchText: String?) {
        repository.searchValue = searchText
    }

    fun loadMore() {
        if (repository.items.size > 0 && !endReached() && !busy) {
            loadResults()
        }
    }

    fun loadResults() {
        viewModelScope.launch(Dispatchers.IO) {
            busy = true
            repository.getLinks()
            withContext(Dispatchers.Main) {
                // Emit new values from repository
                resultsLiveData.value = repository.items.toList()
            }
            busy = false
        }
    }

    fun setCategory(category: NyaaReleaseCategory) {
        this.repository.category = category
    }

    fun setUsername(username: String?) {
        repository.username = username
    }

    fun clearResults() {
        firstInsert = true
        repository.clearRepo()
        resultsLiveData.value = repository.items.toList()
    }

    fun endReached() = this.repository.endReached
}