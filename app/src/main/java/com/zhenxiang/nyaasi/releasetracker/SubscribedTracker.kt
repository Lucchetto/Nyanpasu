package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import java.io.Serializable

@Entity
data class SubscribedTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String? = null,
    val searchQuery: String? = null,
    val category: NyaaReleaseCategory = NyaaReleaseCategory.ALL,
    // lastReleaseTimestamp is expressed in seconds while system timestamps are in milliseconds
    var lastReleaseTimestamp: Long,
    var createdTimestamp: Long,
    var newReleasesCount: Int = 0,
): Serializable
