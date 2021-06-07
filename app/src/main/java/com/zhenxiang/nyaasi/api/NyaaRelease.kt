package com.zhenxiang.nyaasi.api

import java.util.*

class NyaaRelease(
    id: Int, name: String, magnet: String, date: Date, seeders: Int, leechers: Int,
    completed: Int, releaseSize: String, category: NyaaReleaseCategory,
    val user: String?, val hash: String, val descriptionMarkdown: String,
) : NyaaReleasePreviewItem(
    id, name, magnet,
    date,
    seeders,
    leechers, completed, category, releaseSize
) {
}