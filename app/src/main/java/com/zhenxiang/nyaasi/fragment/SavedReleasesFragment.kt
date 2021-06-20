package com.zhenxiang.nyaasi.fragment

import androidx.lifecycle.LiveData
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.db.NyaaReleasePreview

class SavedReleasesFragment: ViewedReleasesFragment() {

    override fun hasDelete(): Boolean {
        return false
    }

    override fun liveDataSource(): LiveData<List<NyaaReleasePreview>> {
        return localNyaaDbViewModel.savedReleases
    }

    override fun searchQuery(query: String?) {
        localNyaaDbViewModel.savedReleasesSearchFilter.value = query
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SavedReleasesFragment().apply {
            }
    }
}