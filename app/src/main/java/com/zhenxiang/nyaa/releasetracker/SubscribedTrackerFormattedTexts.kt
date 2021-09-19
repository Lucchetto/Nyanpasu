package com.zhenxiang.nyaa.releasetracker

import android.content.Context
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.R
import java.text.DateFormat
import java.util.*

data class SubscribedTrackerFormattedTexts(val title: String, val subtitle: String?,
                                           val username: String?, val categoryAndDataSource: String,
                                           val latestRelease: String) {

    companion object {
        fun fromTracker(tracker: SubscribedTracker, context: Context): SubscribedTrackerFormattedTexts {
            val title: String
            val subtitle: String?
            if (tracker.name != null) {
                title = tracker.name
                subtitle = if (tracker.username != null && tracker.searchQuery == null) {
                    context.getString(R.string.tracker_all_releases_from_user, tracker.username)
                } else if (tracker.searchQuery != null) {
                    context.getString(R.string.tracker_keywords, tracker.searchQuery)
                } else {
                    // Else should never happen, unless someone edits the db manually
                    ""
                }
            } else {
                title = if (tracker.username != null && tracker.searchQuery == null) {
                    context.getString(R.string.tracker_all_releases_from_user, tracker.username)
                } else if (tracker.searchQuery != null) {
                    tracker.searchQuery
                } else {
                    // Else should never happen, unless someone edits the db manually
                    ""
                }
                subtitle = null
            }

            // When username is null, we are probably already showing it in title or subtitle
            val username = if (tracker.searchQuery != null && tracker.username != null) {
                context.getString(R.string.release_submitter, tracker.username)
            } else {
                null
            }
            val categoryAndSource = context.getString(R.string.tracker_category_and_source,
                AppUtils.getReleaseCategoryString(context, tracker.dataSourceSpecs.category),
                tracker.dataSourceSpecs.source.url)

            val latestRelease = if (tracker.hasPreviousReleases) {
                context.getString(R.string.tracker_latest_release,
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.latestReleaseTimestamp * 1000))
                )
            } else {
                context.getString(R.string.tracker_no_releases_yet)
            }

            return SubscribedTrackerFormattedTexts(title, subtitle, username, categoryAndSource, latestRelease)
        }
    }
}