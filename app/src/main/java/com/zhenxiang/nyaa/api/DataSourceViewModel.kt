package com.zhenxiang.nyaa.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import kotlinx.coroutines.*

class DataSourceViewModel(application: Application): AndroidViewModel(application) {

    private val repository = NyaaRepository(AppUtils.getUseProxy(application.applicationContext))
    val resultsLiveData = MutableLiveData<List<NyaaReleasePreview>>()
    val error = MutableLiveData<Int>()
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
            val errorCode = repository.getLinks()
            withContext(Dispatchers.Main) {
                // Emit error if not 0 (success)
                if (errorCode != 0) {
                    error.value = errorCode
                } else {
                    // Emit new values from repository
                    resultsLiveData.value = repository.items.toList()
                }
            }
        }
    }

    fun setCategory(category: ReleaseCategory) {
        this.repository.category = category
    }

    fun getCategory(): ReleaseCategory? {
        return this.repository.category
    }

    fun setUsername(username: String?) {
        repository.username = username
    }

    fun setUseProxy(useProxy: Boolean) {
        repository.useProxy = useProxy
    }

    fun clearResults() {
        firstInsert = true
        repository.clearRepo()
        resultsLiveData.value = repository.items.toList()
    }

    fun endReached() = this.repository.endReached
}