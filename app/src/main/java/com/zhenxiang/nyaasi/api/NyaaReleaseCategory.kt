package com.zhenxiang.nyaasi.api

import com.zhenxiang.nyaasi.R

enum class NyaaReleaseCategory(val id: String, val stringResId: Int) {
    ALL("0_0", R.string.category_all),
    ANIME("1_0", R.string.category_anime),
    ANIME_AMV("1_1", R.string.category_anime_amv),
    ANIME_ENGLISH("1_2", R.string.category_anime_english),
    ANIME_NON_ENGLISH("1_3", R.string.category_anime_non_english),
    ANIME_RAW("1_4", R.string.category_anime_raw),
    AUDIO("2_0", R.string.category_audio),
    AUDIO_LOSSLESS("2_1", R.string.category_audio_lossless),
    AUDIO_LOSSY("2_2", R.string.category_audio_lossy),
    LITERATURE("3_0", R.string.category_literature),
    LITERATURE_ENGLISH("3_1", R.string.category_literature_english),
    LITERATURE_NON_ENGLISH("3_2", R.string.category_literature_non_english),
    LITERATURE_RAW("3_3", R.string.category_literature_raw),
    LIVEACTION("4_0", R.string.category_liveaction),
    LIVEACTION_ENGLISH("4_1", R.string.category_liveaction_english),
    LIVEACTION_IDOL_PROM("4_2", R.string.category_liveaction_idol_prom),
    LIVEACTION_NON_ENGLISH("4_3", R.string.category_liveaction_non_english),
    LIVEACTION_RAW("4_4", R.string.category_liveaction_raw),
    PICTURES("5_0", R.string.category_pictures),
    PICTURES_GRAPHICS("5_1", R.string.category_pictures_graphics),
    PICTURES_PHOTOS("5_2", R.string.category_pictures_photos),
    SOFTWARE("6_0", R.string.category_software),
    SOFTWARE_APPS("6_1", R.string.category_software_apps),
    SOFTWARE_GAMES("6_2", R.string.category_software_games),
}
