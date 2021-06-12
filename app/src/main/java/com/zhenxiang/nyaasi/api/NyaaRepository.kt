package com.zhenxiang.nyaasi.api

import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NyaaRepository {

    private val TAG = javaClass.name

    val items = mutableListOf<NyaaReleasePreview>()
    private var pageIndex = 0
    var bottomReached = false
        private set

    var searchValue: String? = null
        set(value) {
            field = value
            clearRepo()
            bottomReached = value == null || value.isEmpty()
        }

    var category: NyaaReleaseCategory = NyaaReleaseCategory.ALL

    fun clearRepo() {
        pageIndex = 0
        items.clear()
        bottomReached = false
    }

    suspend fun getLinks(): Boolean = withContext(Dispatchers.IO) {
        if (!bottomReached) {
            pageIndex++
            val newItems = NyaaPageProvider.getPageItems(pageIndex, category, searchValue)
            newItems?.let {
                bottomReached = if (it.isNotEmpty()) {
                    items.addAll(it)
                    // Prevent loading too many items in the repository
                    items.size > MAX_LOADABLE_ITEMS
                } else {
                    true
                }
            }
        }
        bottomReached
    }

    companion object {
        const val MAX_LOADABLE_ITEMS = 3500
    }
}