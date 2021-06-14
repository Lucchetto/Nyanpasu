package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class SubscribedUser(
    @PrimaryKey val username: String,
    var lastReleaseTimestamp: Long,
): Serializable
