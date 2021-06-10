package com.zhenxiang.nyaasi.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import java.io.Serializable
import java.util.*

@Entity
class NyaaRelease(
    @PrimaryKey val id: Int,
    val name: String,
    val magnet: String,
    val date: Date,
    val seeders: Int,
    val leechers: Int,
    val completed: Int,
    val category: NyaaReleaseCategory,
    val releaseSize: String,
    var user: String? = null,
    var hash: String? = null,
    var descriptionMarkdown: String? = null,
): Serializable
