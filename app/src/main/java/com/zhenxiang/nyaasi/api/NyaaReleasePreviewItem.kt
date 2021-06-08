package com.zhenxiang.nyaasi.api

import java.io.Serializable
import java.util.*

interface NyaaReleaseBase: Serializable {
    val id: Int
    val name: String
    val magnet: String
    val date: Date
    val seeders: Int
    val leechers: Int
    val completed: Int
    val category: NyaaReleaseCategory
    val releaseSize: String
}

class NyaaReleasePreviewItem(
    override val id: Int,
    override val name: String,
    override val magnet: String,
    override val date: Date,
    override val seeders: Int,
    override val leechers: Int,
    override val completed: Int,
    override val category: NyaaReleaseCategory,
    override val releaseSize: String
) : NyaaReleaseBase {}