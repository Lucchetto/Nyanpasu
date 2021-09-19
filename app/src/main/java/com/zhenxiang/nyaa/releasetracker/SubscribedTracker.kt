package com.zhenxiang.nyaa.releasetracker

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaa.api.DataSourceSpecs
import java.io.Serializable

@Entity
data class SubscribedTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String? = null,
    val username: String? = null,
    val searchQuery: String? = null,
    @Embedded val dataSourceSpecs: DataSourceSpecs,
    // latestReleaseTimestamp is expressed in seconds while system timestamps are in milliseconds
    var latestReleaseTimestamp: Long,
    var hasPreviousReleases: Boolean = true,
    var createdTimestamp: Long,
    var newReleasesCount: Int = 0,
): Serializable
