package com.zhenxiang.nyaasi.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class SavedNyaaRelease(
    @PrimaryKey val releaseId: Int,
    val timestamp: Long,
)

data class SavedNyaaReleaseWithDetails(
    @Embedded val savedNyaaRelease: SavedNyaaRelease,
    @Relation(
        parentColumn = "releaseId",
        entityColumn = "id"
    )
    val details: NyaaReleasePreview
)
