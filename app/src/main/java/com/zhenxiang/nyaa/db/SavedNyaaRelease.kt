package com.zhenxiang.nyaa.db

import androidx.room.*

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["id"], childColumns = ["releaseId"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
])
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
