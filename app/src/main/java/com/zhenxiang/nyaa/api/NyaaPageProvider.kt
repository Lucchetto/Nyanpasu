package com.zhenxiang.nyaa.api

import android.util.Log
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.ReleaseId
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.URLEncoder

data class NyaaPageResults(val items: List<NyaaReleasePreview>, val bottomReached: Boolean)

class NyaaPageProvider {

    companion object {
        private val categoryIdRegex = "^\\d+_\\d+\$".toRegex()
        private val TAG = javaClass.name
        suspend fun getPageItems(pageIndex: Int,
                                 category: NyaaReleaseCategory = NyaaReleaseCategory.ALL,
                                 searchQuery: String? = null,
                                 user: String? = null): NyaaPageResults? {
            var url = "https://nyaa.si/"
            if (!user.isNullOrBlank()) {
                url += "user/$user"
            }
            url += "?p=${pageIndex}"
            searchQuery?.let {
                url += "&q=${URLEncoder.encode(it, "utf-8")}"
            }
            if (category != NyaaReleaseCategory.ALL) {
                url += "&c=${category.id}"
            }

            val pageItems: Elements
            val doc: Document
            try {
                doc = Jsoup.connect(url).get()
                 pageItems = doc.select("tr >td > a[href~=^\\/view\\/\\d+\$]")
            } catch (e: Exception) {
                Log.e(TAG, "exception", e)
                return null
            }

            val foundReleases = mutableListOf<NyaaReleasePreview>()
            pageItems.forEach {
                try {
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
                    val releaseSize = parentRow.selectFirst("td:matches(^\\d*\\.?\\d* [a-zA-Z]+\$)").text()

                    val nyaaItem = NyaaReleasePreview(ReleaseId(id, 0), title, magnetLink, timestamp, seeders, leechers, completed, category!!, releaseSize)
                    foundReleases.add(nyaaItem)
                } catch (e: Exception) {}
            }
            val endReached = doc.selectFirst("ul.pagination") == null || doc.selectFirst("ul.pagination > li.next.disabled") != null
            return NyaaPageResults(foundReleases, endReached)
        }
    }
}