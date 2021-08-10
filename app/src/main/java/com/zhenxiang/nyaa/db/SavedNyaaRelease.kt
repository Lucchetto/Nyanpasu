package com.zhenxiang.nyaa.db

import androidx.room.*

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["id", "dataSource"], childColumns = ["releaseId", "releaseDataSource"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["releaseId", "releaseDataSource"])
data class SavedNyaaRelease(
    val releaseId: Int,
    val releaseDataSource: Int,
    val savedTimestamp: Long,
)

data class SavedNyaaReleaseWithDetails(
    @Embedded val savedNyaaRelease: SavedNyaaRelease,
    @Embedded val details: NyaaReleasePreview
)
