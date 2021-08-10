package com.zhenxiang.nyaa.db

import androidx.room.*

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["id", "dataSource"], childColumns = ["releaseId", "releaseDataSource"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["releaseId", "releaseDataSource"])
data class ViewedNyaaRelease(
    val releaseId: Int,
    val releaseDataSource: Int,
    val viewedTimestamp: Long,
)

data class ViewedNyaaReleaseWithDetails(
    @Embedded val viewedNyaaRelease: ViewedNyaaRelease,
    @Embedded val details: NyaaReleasePreview
)
