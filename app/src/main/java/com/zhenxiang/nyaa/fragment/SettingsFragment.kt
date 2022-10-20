package com.zhenxiang.nyaa.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.util.UIModeUtils

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == requireContext().getString(R.string.open_discord_key)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://discord.gg/PDJVtsHJMc")
                startActivity(intent)
            } catch (_: Exception) {}
        }
        return super.onPreferenceTreeClick(preference)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
            }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.ui_mode_key) -> {
                UIModeUtils.updateUIMode(requireContext())
            }
        }
    }
}