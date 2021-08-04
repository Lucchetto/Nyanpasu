package com.zhenxiang.nyaasi.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NyaaBrowseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NyaaRepository()
    val itemsLiveData = MutableLiveData(repository.items)
    private var busy = false

    fun loadMore() {
        if (repository.items.size > 0 && !endReached() && !busy) {
            loadData()
        }
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            busy = true
            // Request more items from repository
            repository.getLinks()
            withContext(Dispatchers.Main) {
                // Emit new values from repository
                itemsLiveData.value = repository.items
            }
            busy = false
        }
    }

    fun setCategory(category: NyaaReleaseCategory) {
        this.repository.category = category
        this.repository.clearRepo()
        itemsLiveData.value = repository.items
    }

    fun endReached() = this.repository.endReached
}