package com.zhenxiang.nyaasi.api

import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NyaaRepository {

    private val TAG = javaClass.name

    val items = mutableListOf<NyaaReleasePreview>()
    private var pageIndex = 0
    var endReached = false
        private set

    var searchValue: String? = null
        set (value) {
            field = if (value?.isBlank() == true) null else value
        }

    var username: String? = null
        set (value) {
            field = if (value?.isBlank() == true) null else value
        }

    var category: NyaaReleaseCategory = NyaaReleaseCategory.ALL

    fun clearRepo() {
        pageIndex = 0
        items.clear()
        endReached = false
    }

    suspend fun getLinks(): Boolean = withContext(Dispatchers.IO) {
        if (!endReached) {
            pageIndex++
            val newItems = NyaaPageProvider.getPageItems(pageIndex, category, searchValue, username)
            newItems?.let {
                items.addAll(it.items)
                endReached = if (it.bottomReached) {
                    true
                } else {
                    // Prevent loading too many items in the repository
                    items.size > MAX_LOADABLE_ITEMS
                }
            }
        }
        endReached
    }

    companion object {
        const val MAX_LOADABLE_ITEMS = 3500
    }
}