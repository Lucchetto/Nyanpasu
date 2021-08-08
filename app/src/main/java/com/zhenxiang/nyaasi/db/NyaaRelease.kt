package com.zhenxiang.nyaasi.db

import androidx.room.*
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import java.io.Serializable
import java.util.*

@Entity
data class NyaaReleasePreview(
    @PrimaryKey val id: Int,
    val name: String,
    val magnet: String,
    // timestamp is expressed in seconds while system timestamps are in milliseconds
    val timestamp: Long,
    val seeders: Int,
    val leechers: Int,
    val completed: Int,
    val category: NyaaReleaseCategory,
    val releaseSize: String,
): Serializable

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["id"], childColumns = ["parentId"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
])
data class NyaaReleaseDetails(
    @PrimaryKey val parentId: Int,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
)
