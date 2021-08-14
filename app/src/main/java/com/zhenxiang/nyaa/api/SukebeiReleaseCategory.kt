package com.zhenxiang.nyaa.api

import com.zhenxiang.nyaa.R

enum class SukebeiReleaseCategory(private val id: String, private val stringResId: Int): ReleaseCategory {
    // ALWAYS PUT DEFAULT CATEGORY AT TOP
    ALL("0_0", R.string.category_all),
    ART("1_0", R.string.category_art),
    ANIME("1_1", R.string.category_anime),
    DOUJINSHI("1_2", R.string.category_doujinshi),
    GAMES("1_3", R.string.category_games),
    MANGA("1_4", R.string.category_manga),
    PICTURES("1_5", R.string.category_pictures),
    REAL_LIFE("2_0", R.string.category_real_life),
    PHOTOBOOKS_PICTURES("2_1", R.string.category_photobooks_pictures),
    VIDEOS("2_2", R.string.category_videos);

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
