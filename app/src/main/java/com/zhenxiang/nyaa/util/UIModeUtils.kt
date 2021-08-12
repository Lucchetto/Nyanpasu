package com.zhenxiang.nyaa.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.zhenxiang.nyaa.R

class UIModeUtils {

    companion object {
        fun updateUIMode(context: Context) {
            AppCompatDelegate.setDefaultNightMode(getUiModeFromPref(context))
        }

        fun getUiModeFromPref(context: Context): Int {
            val prefValue = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.ui_mode_key), null)
            return when (prefValue) {
                "0" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "1" -> AppCompatDelegate.MODE_NIGHT_NO
                "2" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
    }
}