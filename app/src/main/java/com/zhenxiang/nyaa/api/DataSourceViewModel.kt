package com.zhenxiang.nyaa.api

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.ReleaseListParent
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

    fun setUseProxyAndReload(useProxy: Boolean) {
        repository.useProxy = useProxy
        clearResults()
        loadResults()
    }

    fun clearResults() {
        firstInsert = true
        repository.clearRepo()
        resultsLiveData.value = repository.items.toList()
    }

    fun endReached() = this.repository.endReached
}

fun DataSourceViewModel.setupRegionalBlockDetection(parent: ReleaseListParent,
                                                    lifecycleOwner: LifecycleOwner, prefs: SharedPreferences) {
    val useProxyPrefKey = parent.getSnackBarParentView().context.getString(R.string.use_proxy_key)
    this.error.observe(lifecycleOwner, { error ->
        // Potential regional block detected
        if (error == REGIONAL_BLOCK && !prefs.getBoolean(useProxyPrefKey, false)) {

            val snackbar = Snackbar.make(parent.getSnackBarParentView(),
                parent.getSnackBarParentView().context.getString(R.string.connection_reset_error),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction(R.string.turn_on_regional_bypass) {
                prefs.edit().putBoolean(useProxyPrefKey, true).commit()

                // Reload everything
                this.setUseProxyAndReload(true)

                val successSnackbar = Snackbar.make(parent.getSnackBarParentView(),
                    parent.getSnackBarParentView().context.getString(R.string.regional_bypass_turned_on_hint),
                    Snackbar.LENGTH_SHORT
                )
                parent.getSnackBarAnchorView()?.let {
                    successSnackbar.anchorView = it
                }
                successSnackbar.show()
            }
            parent.getSnackBarAnchorView()?.let {
                snackbar.anchorView = it
            }
            snackbar.show()
        }
    })
}