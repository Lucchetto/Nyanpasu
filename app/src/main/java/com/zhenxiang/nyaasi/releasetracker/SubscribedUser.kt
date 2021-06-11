package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SubscribedUser(
    @PrimaryKey val username: String,
    val lastReleaseTimestamp: Long? = null,
)
