package com.zhenxiang.nyaasi.api

import java.util.*

data class NyaaRelease(
    override val id: Int,
    override val name: String,
    override val magnet: String,
    override val date: Date,
    override val seeders: Int,
    override val leechers: Int,
    override val completed: Int,
    override val category: NyaaReleaseCategory,
    override val releaseSize: String,
    val user: String?,
    val hash: String,
    val descriptionMarkdown: String,
) : NyaaReleaseBase {}