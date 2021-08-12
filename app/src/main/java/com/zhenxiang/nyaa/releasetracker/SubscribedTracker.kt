package com.zhenxiang.nyaa.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.ReleaseCategory
import java.io.Serializable

@Entity
data class SubscribedTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dataSource: ApiDataSource,
    val username: String? = null,
    val searchQuery: String? = null,
    val category: ReleaseCategory? = null,
    // latestReleaseTimestamp is expressed in seconds while system timestamps are in milliseconds
    var latestReleaseTimestamp: Long,
    var hasPreviousReleases: Boolean = true,
    var createdTimestamp: Long,
    var newReleasesCount: Int = 0,
): Serializable