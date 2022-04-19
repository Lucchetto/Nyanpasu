package com.zhenxiang.nyaa.model

import com.zhenxiang.nyaa.api.ReleaseCategory

data class SearchSpecsModel(
    var category: ReleaseCategory? = null,
    var pageIndex: Int = 1,
    var searchQuery: String? = null,
    var username: String? = null,
)
