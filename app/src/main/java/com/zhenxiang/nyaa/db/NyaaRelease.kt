package com.zhenxiang.nyaa.db

import androidx.room.*
import com.zhenxiang.nyaa.api.DataSourceSpecs
import com.zhenxiang.nyaa.api.ReleaseComment
import com.zhenxiang.nyaa.api.ReleaseId
import java.io.Serializable

@Entity(primaryKeys = ["number", "dataSource"])
data class NyaaReleasePreview(
    val number: Int,
    @Embedded val dataSourceSpecs: DataSourceSpecs,
    val name: String,
    val magnet: String,
    // timestamp is expressed in seconds while system timestamps are in milliseconds
    val timestamp: Long,
    val seeders: Int,
    val leechers: Int,
    val completed: Int,
    val releaseSize: String,
): Serializable {
    companion object {
        fun NyaaReleasePreview.getReleaseId(): ReleaseId {
            return ReleaseId(this.number, this.dataSourceSpecs.source)
        }
    }
}

@Entity(foreignKeys = [
    ForeignKey(entity = NyaaReleasePreview::class,
        parentColumns = ["number", "dataSource"], childColumns = ["parent_number", "parent_dataSource"],
        onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
], primaryKeys = ["parent_number", "parent_dataSource"]
) data class NyaaReleaseDetails(
    @Embedded(prefix = "parent_") val releaseId: ReleaseId,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
    @Ignore
    val comments: List<ReleaseComment>?,
) {
    constructor(releaseId: ReleaseId,
                user: String?,
                hash: String,
                descriptionMarkdown: String): this(releaseId, user, hash, descriptionMarkdown, null)
}
