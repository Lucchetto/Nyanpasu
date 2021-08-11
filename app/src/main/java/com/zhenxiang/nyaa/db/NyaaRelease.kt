package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.ReleaseCategory
import java.io.Serializable

@Entity(primaryKeys = ["number", "dataSource"])
data class NyaaReleasePreview(
    @Embedded val id: ReleaseId,
    val name: String,
    val magnet: String,
    // timestamp is expressed in seconds while system timestamps are in milliseconds
    val timestamp: Long,
    val seeders: Int,
    val leechers: Int,
    val completed: Int,
    val category: ReleaseCategory?,
    val releaseSize: String,
): Serializable

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["number", "dataSource"], childColumns = ["parent_number", "parent_dataSource"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["parent_number", "parent_dataSource"])
data class NyaaReleaseDetails(
    @Embedded(prefix = "parent_") val releaseId: ReleaseId,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
)
