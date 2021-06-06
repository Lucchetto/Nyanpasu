package com.zhenxiang.nyaasi.api

import java.io.Serializable
import java.util.*

class NyaaReleaseItem(val id: Int, val name: String, val magnet: String, val date: Date,
                      val seeders: Int, val leechers: Int, val completed: Int): Serializable {
}