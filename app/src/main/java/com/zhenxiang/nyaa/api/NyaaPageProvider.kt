package com.zhenxiang.nyaa.api

import android.net.Uri
import android.util.Log
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

data class NyaaPageResults(val items: List<NyaaReleasePreview>, val bottomReached: Boolean)

class NyaaPageProvider {

    companion object {
        private val categoryIdRegex = "([^(.*?)(\\?|(%3F))c(=|(%3D))])*\$".toRegex()
        private val releaseIdRegex = "[^(.*?)(\\/|(%2F))view(\\/|(%2F))]\\d+\$".toRegex()
        private val TAG = javaClass.name

        suspend fun getReleaseDetails(releaseId: ReleaseId, useProxy: Boolean): NyaaReleaseDetails? {
            return try {
                val doc: Document = Jsoup.connect(AppUtils.getReleasePageUrl(releaseId, useProxy)).get()
                doc.outputSettings().prettyPrint(false)

                val userName = doc.selectFirst("div.col-md-1:matches(Submitter:)").parent().select("a[href~=(.*?)([\\/]|(%2F))user([\\/]|(%2F))(.+)\$]").text()
                val hash = doc.selectFirst("div.col-md-1:matches(Info hash:)").parent().select("kbd:matches(^(\\w{40})\$)").text()
                val descriptionMarkdown = doc.getElementById("torrent-description").html()

                val comments = mutableListOf<ReleaseComment>()
                val commentsContainer = doc.getElementById("collapse-comments")
                commentsContainer?.select("*[id~=^com-\\d+\$]")?.forEach {
                    val commentUsername = it.selectFirst("a[href~=(.*?)([\\/]|(%2F))user([\\/]|(%2F))(.+)\$]").text()
                    val commentImage = it.selectFirst("img").attr("src")
                    val timestamp = it.selectFirst("*[data-timestamp~=^\\d+\$]").attr("data-timestamp").toLong()
                    val commentContent = Parser.unescapeEntities(it.select(".comment-content").html(), true)
                    comments.add(ReleaseComment(commentUsername, commentImage, timestamp, commentContent))
                }

                NyaaReleaseDetails(releaseId, if (userName.isNullOrBlank()) null else userName, hash, descriptionMarkdown, comments)
            } catch(e: Exception) {
                Log.w(TAG, e)
                null
            }
        }

        suspend fun getPageItems(dataSource: ApiDataSource,
                                 useProxy: Boolean,
                                 pageIndex: Int,
                                 category: ReleaseCategory? = null,
                                 searchQuery: String? = null,
                                 user: String? = null): NyaaPageResults {

            var fullUrl = "https://${if (useProxy) dataSource.proxyUrl else dataSource.url}%2F"
            if (!user.isNullOrBlank()) {
                fullUrl += "user%2F$user"
            }
            fullUrl += "%3Fp%3D${pageIndex}"
            searchQuery?.let {
                fullUrl += "%26q%3D${URLEncoder.encode(it, "utf-8")}"
            }
            if (category != null) {
                fullUrl += "%26c%3D${category.getId()}"
            }

            val pageItems: Elements
            val doc: Document
            try {
                doc = Jsoup.connect(fullUrl).get()
                pageItems = doc.select("tr >td > a[href~=(.*?)([\\/]|(%2F))view([\\/]|(%2F))\\d+\$]")
            } catch (e: Exception) {
                Log.e(TAG, "exception", e)
                throw (e)
            }

            val foundReleases = mutableListOf<NyaaReleasePreview>()
            pageItems.forEach {
                try {
                    // Get parent tr since we select element by a
                    val parentRow = it.parent().parent()

                    val categoryHref =  parentRow.selectFirst("td > a[href~=(.*?)(\\?|(%3F))c(=|(%3D))\\d+_\\d+\$]").attr("href")
                    val category = DataSourceSpecs.getCategoryFromId(dataSource, categoryIdRegex.find(categoryHref)!!.groupValues[0])

                    val number = releaseIdRegex.find(it.attr("href"))!!.groupValues[0].toInt()
                    val title = it.attr("title")
                    val magnetLink = parentRow.selectFirst("a[href~=^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$]").attr("href")
                    val timestamp = parentRow.selectFirst("*[data-timestamp~=^\\d+\$]").attr("data-timestamp").toString().toLong()
                    val seeders = parentRow.select("td:nth-child(6)").text().toInt()
                    val leechers = parentRow.select("td:nth-child(7)").text().toInt()
                    val completed = parentRow.select("td:nth-child(8)").text().toInt()
                    val releaseSize = parentRow.selectFirst("td:matches(^\\d*\\.?\\d* [a-zA-Z]+\$)").text()

                    val nyaaItem = NyaaReleasePreview(number, DataSourceSpecs(dataSource, category), title, magnetLink, timestamp, seeders, leechers, completed, releaseSize)
                    foundReleases.add(nyaaItem)
                } catch (e: Exception) {
                    Log.w(TAG, e)
                }
            }
            val endReached = doc.selectFirst("ul.pagination") == null || doc.selectFirst("ul.pagination > li.next.disabled") != null
            return NyaaPageResults(foundReleases, endReached)
        }
    }
}