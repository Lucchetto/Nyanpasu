package com.zhenxiang.nyaa.fragment

import androidx.lifecycle.LiveData
import com.zhenxiang.nyaa.R
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

    override fun emptyViewStringRes(): Int {
        return R.string.empty_saved_view_hint
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SavedReleasesFragment().apply {
            }
    }
}