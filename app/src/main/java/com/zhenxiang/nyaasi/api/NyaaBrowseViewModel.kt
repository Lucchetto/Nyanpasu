package com.zhenxiang.nyaasi.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NyaaBrowseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NyaaRepository()
    val itemsLiveData = MutableLiveData<List<NyaaReleasePreview>>()
    private var busy = false
    var firstInsert: Boolean = true

    fun loadMore() {
        firstInsert = false
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