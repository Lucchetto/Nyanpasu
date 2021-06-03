package com.zhenxiang.nyaasi.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.select.Elements

class NyaaViewModel: ViewModel() {

    val repository = NyaaRepository()
    val itemsLiveData = MutableLiveData(repository.items)

    private var bottomReached = false

    suspend fun loadData() {
        // Request more items from repository
        bottomReached = repository.getLinks()
        withContext(Dispatchers.Main) {
            // Emit new values from repository
            itemsLiveData.value = repository.items
        }
    }

    fun isBottomReached() = bottomReached
}