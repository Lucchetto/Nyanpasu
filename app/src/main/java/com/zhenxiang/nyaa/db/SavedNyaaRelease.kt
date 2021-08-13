package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.ReleaseId

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["number", "dataSource"], childColumns = ["parent_number", "parent_dataSource"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["parent_number", "parent_dataSource"])
data class SavedNyaaRelease(
    @Embedded(prefix = "parent_") val releaseId: ReleaseId,
    val savedTimestamp: Long,
)

data class SavedNyaaReleaseWithDetails(
    @Embedded val savedNyaaRelease: SavedNyaaRelease,
    @Embedded val details: NyaaReleasePreview
)
