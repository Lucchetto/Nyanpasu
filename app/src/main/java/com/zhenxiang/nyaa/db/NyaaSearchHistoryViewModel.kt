package com.zhenxiang.nyaa.db

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.zhenxiang.nyaa.api.ApiDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NyaaSearchHistoryViewModel(application: Application): AndroidViewModel(application) {
    private val dao = NyaaDb(application.applicationContext).nyaaSearchHistoryDao()

    val searchHistoryFilter = MutableLiveData<String>()
    val searchHistoryDataSource = MutableLiveData<ApiDataSource>()
    // Source of data
    private val preFilterSearchHistory = dao.getAllLive()

    val searchHistory = Transformations.switchMap(DoubleTrigger(searchHistoryFilter, searchHistoryDataSource)) { pair ->
            Transformations.map(preFilterSearchHistory) {
                it.filter { item ->
                    pair.first?.let {
                            searchQuery -> item.searchQuery.contains(searchQuery, true)
                    } ?:run {
                        true
                    } && pair.second?.let { dataSource ->
                         item.dataSource == null || dataSource == item.dataSource
                    } ?: run {
                        true
                    }
                }
            }
        }

    fun insert(item: NyaaSearchHistoryItem) {
        val formattedItem = NyaaSearchHistoryItem(item.searchQuery.trim(), item.searchTimestamp, item.dataSource)
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(formattedItem)
            dao.deleteExcessiveRecents()
        }
    }

    fun delete(item: NyaaSearchHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(item)
        }
    }
}

class DoubleTrigger<A, B>(a: LiveData<A>, b: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
    init {
        addSource(a) { value = it to b.value }
        addSource(b) { value = a.value to it }
    }
}
