package com.zhenxiang.nyaasi.db

import androidx.room.*

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["id"], childColumns = ["releaseId"],
        onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
])
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
