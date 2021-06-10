package com.zhenxiang.nyaasi.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ViewedNyaaRelease(
    @PrimaryKey val releaseId: Int,
    val timestamp: Long,
)

data class ViewedNyaaReleaseWithDetails(
    @Embedded val viewedNyaaRelease: ViewedNyaaRelease,
    @Relation(
        parentColumn = "releaseId",
        entityColumn = "id"
    )
    val details: NyaaReleasePreview
)
