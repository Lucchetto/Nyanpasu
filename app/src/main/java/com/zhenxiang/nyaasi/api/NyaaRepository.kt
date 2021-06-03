package com.zhenxiang.nyaasi.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

class NyaaRepository {

    private val TAG = javaClass.name

    val items = mutableListOf<NyaaDownloadItem>()
    private var pageIndex = 0
    private var bottomReached = false

    suspend fun getLinks(): Boolean = withContext(Dispatchers.Default) {
        if (!bottomReached) {
            pageIndex++
            try {
                val doc: Document = Jsoup.connect("https://nyaa.si/?p=${pageIndex}").get()
                // Check that item has href with format /view/[integer_id]
                val pageItems = doc.select("tr>td>a[href~=^\\/view\\/\\d+\$]")
                if (pageItems.size > 0) {
                    pageItems.forEach {
                        // Get parent tr since we select element by a
                        val parentRow = it.parent().parent()
                        val id = it.attr("href").split("/").last().toInt()
                        val title = it.attr("title")
                        val magnetLink = parentRow.selectFirst("a[href~=^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$]").attr("href").toString()
                        val timestamp = parentRow.selectFirst("*[data-timestamp~=^\\d+\$]").attr("data-timestamp").toString().toLong()
                        val nyaaItem = NyaaDownloadItem(id, title, magnetLink, Date(timestamp * 1000))
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
        const val MAX_LOADABLE_ITEMS = 150
    }
}