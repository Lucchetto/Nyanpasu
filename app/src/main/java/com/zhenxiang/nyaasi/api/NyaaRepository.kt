package com.zhenxiang.nyaasi.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.*

class NyaaRepository {

    private val TAG = javaClass.name
    private val categoryIdRegex = "^\\d+_\\d+\$".toRegex()

    val items = mutableListOf<NyaaReleasePreviewItem>()
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
            try {
                var url = "https://nyaa.si/?p=${pageIndex}"
                searchValue?.let {
                    url += "&q=${URLEncoder.encode(it, "utf-8")}"
                }
                if (category != NyaaReleaseCategory.ALL) {
                    url += "&c=${category.id}"
                }
                val doc: Document = Jsoup.connect(url).get()
                // Check that item has href with format /view/[integer_id]
                val pageItems = doc.select("tr >td > a[href~=^\\/view\\/\\d+\$]")
                if (pageItems.size > 0) {
                    pageItems.forEach {
                        // Get parent tr since we select element by a
                        val parentRow = it.parent().parent()

                        val categoryId = categoryIdRegex.find(parentRow.selectFirst("td > a[href~=^(.*?)(\\?|\\&)c=\\d+_\\d+\$]").attr("href").removePrefix("/?c="))!!.value
                        val category = NyaaReleaseCategory.values().find { category -> category.id == categoryId }

                        val id = it.attr("href").split("/").last().toInt()
                        val title = it.attr("title")
                        val magnetLink = parentRow.selectFirst("a[href~=^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$]").attr("href")
                        val timestamp = parentRow.selectFirst("*[data-timestamp~=^\\d+\$]").attr("data-timestamp").toString().toLong()
                        val seeders = parentRow.select("td:nth-child(6)").text().toInt()
                        val leechers = parentRow.select("td:nth-child(7)").text().toInt()
                        val completed = parentRow.select("td:nth-child(8)").text().toInt()

                        val nyaaItem = NyaaReleasePreviewItem(id, title, magnetLink, Date(timestamp * 1000), seeders, leechers, completed, category!!)
                        items.add(nyaaItem)

                        // Prevent loading too many items in the repository
                        bottomReached = items.size > MAX_LOADABLE_ITEMS
                    }
                } else {
                    // If pageItems size is 0 we probably reached the end
                    bottomReached = true
                }
            } catch(e: Exception) {
                Log.e(TAG, "exception", e)
            }
        }
        bottomReached
    }

    companion object {
        const val MAX_LOADABLE_ITEMS = 3500
    }
}