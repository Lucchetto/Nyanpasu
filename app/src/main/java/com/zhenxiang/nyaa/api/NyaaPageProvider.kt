package com.zhenxiang.nyaa.api

import android.util.Log
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.URLEncoder

data class NyaaPageResults(val items: List<NyaaReleasePreview>, val bottomReached: Boolean)

class NyaaPageProvider {

    companion object {
        private val categoryIdRegex = "^\\d+_\\d+\$".toRegex()
        private val TAG = javaClass.name

        suspend fun getReleaseDetails(releaseId: ReleaseId): NyaaReleaseDetails? {
            return try {
                val doc: Document = Jsoup.connect(AppUtils.getReleasePageUrl(releaseId)).get()
                doc.outputSettings().prettyPrint(false)

                val userName = doc.selectFirst("div.col-md-1:matches(Submitter:)").parent().select("a[href~=^(.*?)\\/user\\/(.+)\$]").text()
                val hash = doc.selectFirst("div.col-md-1:matches(Info hash:)").parent().select("kbd:matches(^(\\w{40})\$)").text()
                val descriptionMarkdown = doc.getElementById("torrent-description").html()

                NyaaReleaseDetails(releaseId, if (userName.isNullOrBlank()) null else userName, hash, descriptionMarkdown)
            } catch(e: Exception) {
                Log.w(TAG, e)
                null
            }
        }

        suspend fun getPageItems(dataSource: ApiDataSource,
                                 pageIndex: Int,
                                 category: ReleaseCategory? = null,
                                 searchQuery: String? = null,
                                 user: String? = null): NyaaPageResults {

            var fullUrl = "https://${dataSource.url}/"
            if (!user.isNullOrBlank()) {
                fullUrl += "user/$user"
            }
            fullUrl += "?p=${pageIndex}"
            searchQuery?.let {
                fullUrl += "&q=${URLEncoder.encode(it, "utf-8")}"
            }
            if (category != null) {
                fullUrl += "&c=${category.getId()}"
            }

            val pageItems: Elements
            val doc: Document
            try {
                doc = Jsoup.connect(fullUrl).get()
                 pageItems = doc.select("tr >td > a[href~=^\\/view\\/\\d+\$]")
            } catch (e: Exception) {
                Log.e(TAG, "exception", e)
                throw (e)
            }

            val foundReleases = mutableListOf<NyaaReleasePreview>()
            pageItems.forEach {
                try {
                    // Get parent tr since we select element by a
                    val parentRow = it.parent().parent()

                    val categoryId = categoryIdRegex.find(parentRow.selectFirst("td > a[href~=^(.*?)(\\?|\\&)c=\\d+_\\d+\$]").attr("href").removePrefix("/?c="))!!.value
                    val category = DataSourceSpecs.getCategoryFromId(dataSource, categoryId)

                    val number = it.attr("href").split("/").last().toInt()
                    val title = it.attr("title")
                    val magnetLink = parentRow.selectFirst("a[href~=^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$]").attr("href")
                    val timestamp = parentRow.selectFirst("*[data-timestamp~=^\\d+\$]").attr("data-timestamp").toString().toLong()
                    val seeders = parentRow.select("td:nth-child(6)").text().toInt()
                    val leechers = parentRow.select("td:nth-child(7)").text().toInt()
                    val completed = parentRow.select("td:nth-child(8)").text().toInt()
                    val releaseSize = parentRow.selectFirst("td:matches(^\\d*\\.?\\d* [a-zA-Z]+\$)").text()

                    val nyaaItem = NyaaReleasePreview(number, DataSourceSpecs(dataSource, category), title, magnetLink, timestamp, seeders, leechers, completed, releaseSize)
                    foundReleases.add(nyaaItem)
                } catch (e: Exception) {}
            }
            val endReached = doc.selectFirst("ul.pagination") == null || doc.selectFirst("ul.pagination > li.next.disabled") != null
            return NyaaPageResults(foundReleases, endReached)
        }
    }
}