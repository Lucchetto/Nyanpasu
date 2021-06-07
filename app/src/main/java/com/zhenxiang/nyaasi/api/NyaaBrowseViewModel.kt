package com.zhenxiang.nyaasi.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.select.Elements

class NyaaBrowseViewModel: ViewModel() {

    private val repository = NyaaRepository()
    val itemsLiveData = MutableLiveData(repository.items)
    private var busy = false

    fun loadMore() {
        if (repository.items.size > 0 && !isBottomReached() && !busy) {
            loadData()
        }
    }

    fun loadData() {
        viewModelScope.launch() {
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

    fun isBottomReached() = this.repository.bottomReached
}