package com.zhenxiang.nyaa.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import kotlinx.coroutines.*

class NyaaApiViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val resultsLiveData = MutableLiveData<List<NyaaReleasePreview>>()
    var firstInsert: Boolean = true

    fun setSearchText(searchText: String?) {
        repository.searchValue = searchText
    }

    fun loadMore() {
        if (repository.items.size > 0 && !endReached()) {
            loadFromRepo()
        }
    }

    fun loadResults() {
        firstInsert = true
        repository.clearRepo()
        loadFromRepo()
    }

    private fun loadFromRepo() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLinks()
            withContext(Dispatchers.Main) {
                // Emit new values from repository
                resultsLiveData.value = repository.items.toList()
            }
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