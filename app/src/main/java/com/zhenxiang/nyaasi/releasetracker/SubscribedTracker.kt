package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import java.io.Serializable

@Entity
open class SubscribedTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    open val username: String? = null,
    open val searchQuery: String? = null,
    val category: NyaaReleaseCategory = NyaaReleaseCategory.ALL,
    // lastReleaseTimestamp is expressed in seconds while system timestamps are in milliseconds
    var lastReleaseTimestamp: Long,
    var createdTimestamp: Long,
): Serializable

class SubscribedUser(id: Int,
                     override val username: String,
                     category: NyaaReleaseCategory,
                     lastReleaseTimestamp: Long,
                     createdTimestamp: Long,
): SubscribedTracker(id, username, null, category, lastReleaseTimestamp, createdTimestamp)

class SubscribedRelease(id: Int,
                     username: String?,
                     override val searchQuery: String,
                     category: NyaaReleaseCategory,
                     lastReleaseTimestamp: Long,
                     createdTimestamp: Long,
): SubscribedTracker(id, username, searchQuery, category, lastReleaseTimestamp, createdTimestamp)
