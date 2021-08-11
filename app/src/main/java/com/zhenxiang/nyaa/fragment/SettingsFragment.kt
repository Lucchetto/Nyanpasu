package com.zhenxiang.nyaa.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.zhenxiang.nyaa.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
            }
    }
}