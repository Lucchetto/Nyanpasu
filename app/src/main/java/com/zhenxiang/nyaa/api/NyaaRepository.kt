package com.zhenxiang.nyaa.api

import com.zhenxiang.nyaa.db.NyaaReleasePreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

const val PAGE_NOT_FOUND = 404
const val REGIONAL_BLOCK = -40
const val GENERIC_JSOUP_ERROR = -1

class NyaaRepository(var useProxy: Boolean) {

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

    var category: ReleaseCategory? = null

    fun clearRepo() {
        pageIndex = 0
        items.clear()
        endReached = false
    }

    suspend fun getLinks(): Int = withContext(Dispatchers.IO) {
        val category = category
        if (!endReached && category != null) {
            pageIndex++
            try {
                val newItems = NyaaPageProvider.getPageItems(category.getDataSource(), useProxy, pageIndex, category, searchValue, username)
                items.addAll(newItems.items)
                endReached = if (newItems.bottomReached) {
                    true
                } else {
                    // Prevent loading too many items in the repository
                    items.size > MAX_LOADABLE_ITEMS
                }
            } catch (e: Exception) {
                // With an error we can't continue. So mark it as data ended
                endReached = true

                return@withContext if (e is HttpStatusException && e.statusCode == 404) {
                    PAGE_NOT_FOUND
                } else if ((e is SocketException && e.message == "Connection reset")
                    || (e is SSLHandshakeException && e.message?.contains("Connection reset by peer", true) == true)) {
                    REGIONAL_BLOCK
                } else {
                    GENERIC_JSOUP_ERROR
                }
            }
        }
        // Return 0 if everything when fine
        0
    }

    companion object {
        const val MAX_LOADABLE_ITEMS = 3500
    }
}