package com.zhenxiang.nyaa.db

import com.zhenxiang.nyaa.api.ApiDataSource
import java.io.Serializable

data class ReleaseId(val number: Int, val dataSource: ApiDataSource): Serializable