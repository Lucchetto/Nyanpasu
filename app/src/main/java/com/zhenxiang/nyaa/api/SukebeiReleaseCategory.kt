package com.zhenxiang.nyaa.api

import com.zhenxiang.nyaa.R

enum class SukebeiReleaseCategory(private val id: String, private val stringResId: Int): ReleaseCategory {
    // ALWAYS PUT DEFAULT CATEGORY AT TOP
    ALL("0_0", R.string.category_all),
    ART("1_0", R.string.category_art);

    override fun getDataSource(): ApiDataSource {
        return ApiDataSource.SUKEBEI_NYAA_SI
    }

    override fun getId(): String {
        return id
    }

    override fun getStringResId(): Int {
        return stringResId
    }
}
