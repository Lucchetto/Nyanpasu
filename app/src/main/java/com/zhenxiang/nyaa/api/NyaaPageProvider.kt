package com.zhenxiang.nyaa.api

import android.util.Log
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.db.NyaaReleaseDetails
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.model.SearchSpecsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.net.URLEncoder

data class NyaaPageResults(val items: List<NyaaReleasePreview>, val bottomReached: Boolean)

class NyaaPageProvider {

    companion object {

        private const val categoryIdRegexString = "(?<=c(%3D)|=)(\\d+_\\d+)(?=(&.*|\$))"
        @JvmStatic private val categoryIdRegex = categoryIdRegexString.toRegex()
        private const val releaseIdRegexString = "(?<=view(%2F)|\\/)(\\d+)(?=(&.*|\$))"
        @JvmStatic private val releaseIdRegex = releaseIdRegexString.toRegex()
        private const val userRegexString = "(?<=view(%2F)|\\/)(.+)(?=(&.*|$))"
        @JvmStatic private val TAG = javaClass.name

        fun getProperUrl(dataSource: ApiDataSource): String {
            return dataSource.url
        }

        suspend fun getReleaseDetails(releaseId: ReleaseId): NyaaReleaseDetails? {
            return try {
                val doc: Document = Jsoup.connect(AppUtils.getReleasePageUrl(releaseId)).get()
                doc.outputSettings().prettyPrint(false)

                val userName = doc.selectFirst("div.col-md-1:matches(Submitter:)")!!.parent()!!.select("a[href~=$userRegexString]").text()
                val hash = doc.selectFirst("div.col-md-1:matches(Info hash:)")!!.parent()!!.select("kbd:matches(^(\\w{40})\$)").text()
                val descriptionMarkdown = doc.getElementById("torrent-description")!!.html()

                val comments = mutableListOf<ReleaseComment>()
                val commentsContainer = doc.getElementById("collapse-comments")
                commentsContainer?.select("*[id~=^com-\\d+\$]")?.forEach {
                    val commentUsername = it.selectFirst("a[href~=$userRegexString]")!!.text()
                    val commentUrlImage = it.selectFirst("img")!!.absUrl("src")
                    val timestamp = it.selectFirst("*[data-timestamp~=^\\d+\$]")!!.attr("data-timestamp").toLong()
                    val commentContent = Parser.unescapeEntities(it.select(".comment-content").html(), true)
                    comments.add(ReleaseComment(commentUsername, commentUrlImage, timestamp, commentContent))
                }

                NyaaReleaseDetails(releaseId, if (userName.isNullOrBlank()) null else userName, hash, descriptionMarkdown, comments)
            } catch(e: Exception) {
                Log.w(TAG, e)
                null
            }
        }

        suspend fun runSearch(searchSpecsModel: SearchSpecsModel): NyaaPageResults {
            val category = searchSpecsModel.category ?: throw IllegalStateException("Category must not be null")
            return getPageItems(
                category.getDataSource(),
                searchSpecsModel.pageIndex,
                category,
                searchSpecsModel.searchQuery,
                searchSpecsModel.username,
            )
        }

        suspend fun getPageItems(dataSource: ApiDataSource,
                                 pageIndex: Int,
                                 category: ReleaseCategory? = null,
                                 searchQuery: String? = null,
                                 user: String? = null): NyaaPageResults {

            val hostUrl = "https://${getProperUrl(dataSource)}/"

            var pathAndParams = ""
            if (!user.isNullOrBlank()) {
                pathAndParams += "user/$user"
            }
            pathAndParams += "?p=${pageIndex}"
            searchQuery?.let {
                pathAndParams += "&q=${URLEncoder.encode(it, "utf-8")}"
            }
            if (category != null) {
                pathAndParams += "&c=${category.getId()}"
            }

            val pageItems: Elements
            val doc: Document
            try {
                doc = Jsoup.connect(hostUrl + pathAndParams).get()
                pageItems = doc.select("tr >td > a[href~=$releaseIdRegexString]")
            } catch (e: Exception) {
                Log.e(TAG, "exception", e)
                throw (e)
            }

            val foundReleases = mutableListOf<NyaaReleasePreview>()
            pageItems.forEach {
                try {
                    // Get parent tr since we select element by a
                    val parentRow = it.parent()!!.parent()!!

                    val categoryHref =  parentRow.selectFirst("td > a[href~=$categoryIdRegexString]")!!.attr("href")
                    val category = DataSourceSpecs.getCategoryFromId(dataSource, categoryIdRegex.find(categoryHref)!!.groupValues[0])

                    val number = releaseIdRegex.find(it.attr("href"))!!.groupValues[0].toInt()
                    val title = it.attr("title")
                    val magnetLink = parentRow.selectFirst("a[href~=^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$]")!!.attr("href")
                    val timestamp = parentRow.selectFirst("*[data-timestamp~=^\\d+\$]")!!.attr("data-timestamp").toString().toLong()
                    val seeders = parentRow.select("td:nth-child(6)").text().toInt()
                    val leechers = parentRow.select("td:nth-child(7)").text().toInt()
                    val completed = parentRow.select("td:nth-child(8)").text().toInt()
                    val releaseSize = parentRow.selectFirst("td:matches(^\\d*\\.?\\d* [a-zA-Z]+\$)")!!.text()

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