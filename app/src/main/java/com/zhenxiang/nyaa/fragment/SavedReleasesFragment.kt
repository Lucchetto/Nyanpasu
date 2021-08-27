package com.zhenxiang.nyaa.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.db.NyaaReleasePreview

class SavedReleasesFragment: ViewedReleasesFragment() {

    override fun hasDelete(): Boolean {
        return false
    }

    override fun liveDataSource(): LiveData<List<NyaaReleasePreview>> {
        return localNyaaDbViewModel.savedReleases
    }

    override fun searchQueryLiveData(): MutableLiveData<String> {
        return localNyaaDbViewModel.savedReleasesSearchFilter
    }

    override fun emptyViewStringRes(): Int {
        return R.string.empty_saved_view_hint
    }

    override fun emptyViewDrawableRes(): Int {
        return R.drawable.ic_outline_bookmarks_24
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SavedReleasesFragment().apply {
            }
    }
}