package com.zhenxiang.nyaasi.releasetracker

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = SubscribedTracker::class,
        parentColumns = ["id"], childColumns = ["trackerId"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
])
data class NewRelease(
    @PrimaryKey val releaseId: Int,
    val trackerId: Int,
)