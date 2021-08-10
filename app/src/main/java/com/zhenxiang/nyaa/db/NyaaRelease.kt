package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import java.io.Serializable

@Entity(primaryKeys = ["id", "dataSource"])
data class NyaaReleasePreview(
    val id: Int,
    val dataSource: Int,
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
        parentColumns = ["id", "dataSource"], childColumns = ["parentId", "parentDataSource"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["parentId", "parentDataSource"])
data class NyaaReleaseDetails(
    val parentId: Int,
    val parentDataSource: Int,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
)
