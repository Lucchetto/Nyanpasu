package com.zhenxiang.nyaa.fragment

import androidx.lifecycle.LiveData
import com.zhenxiang.nyaa.db.NyaaReleasePreview

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