package com.zhenxiang.nyaasi.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ViewedNyaaRelease(
    @PrimaryKey(autoGenerate = true) val viewedId: Int = 0,
    val releaseId: Int
)

data class ViewedNyaaReleaseWithDetails(
    @Embedded val release: NyaaRelease,
    @Relation(
        parentColumn = "id",
        entityColumn = "releaseId"
    )
    val viewedNyaaRelease: ViewedNyaaRelease
)
