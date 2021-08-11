package com.zhenxiang.nyaa.db

import androidx.room.*

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["number", "dataSource"], childColumns = ["parent_number", "parent_dataSource"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["parent_number", "parent_dataSource"])
data class ViewedNyaaRelease(
    @Embedded(prefix = "parent_") val releaseId: ReleaseId,
    val viewedTimestamp: Long,
)

data class ViewedNyaaReleaseWithDetails(
    @Embedded val viewedNyaaRelease: ViewedNyaaRelease,
    @Embedded val details: NyaaReleasePreview
)
