package com.zhenxiang.nyaa.model

import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.ReleaseCategory

data class SearchSpecsModel(
    var dataSource: ApiDataSource,
    var pageIndex: Int = 1,
    var searchQuery: String? = null,
    var username: String? = null,
    var category: ReleaseCategory? = null,
)
