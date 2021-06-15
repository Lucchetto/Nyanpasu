package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class SubscribedTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String? = null,
    val searchQuery: String? = null,
    var lastReleaseTimestamp: Long,
): Serializable

data class SubscribedUser(
    val id: Int,
    val username: String,
    var lastReleaseTimestamp: Long,
)

data class SubscribedRelease(
    val id: Int,
    val username: String? = null,
    val searchQuery: String,
    var lastReleaseTimestamp: Long,
)
