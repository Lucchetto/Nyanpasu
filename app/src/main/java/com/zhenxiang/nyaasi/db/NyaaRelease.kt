package com.zhenxiang.nyaasi.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.api.NyaaReleasePreviewItem
import java.util.*

@Entity
class NyaaRelease(
    @PrimaryKey override val id: Int,
    name: String,
    magnet: String,
    date: Date,
    seeders: Int,
    leechers: Int,
    completed: Int,
    category: NyaaReleaseCategory,
    releaseSize: String,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
) : NyaaReleasePreviewItem(id, name, magnet, date, seeders, leechers, completed, category, releaseSize) {}