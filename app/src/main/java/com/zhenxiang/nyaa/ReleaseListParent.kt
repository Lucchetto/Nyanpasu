package com.zhenxiang.nyaa

import android.view.View
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaReleasePreview

interface ReleaseListParent: ActivityResultCaller {
    fun getQueuedDownload(): ReleaseId?

    fun getSnackBarParentView(): View

    fun getSnackBarAnchorView(): View?

    fun setQueuedDownload(releaseId: ReleaseId?)

    fun storagePermissionRequestLauncher(): ActivityResultLauncher<String>

    fun getCurrentActivity(): FragmentActivity

    companion object {
        fun setupReleaseListListener(listParent: ReleaseListParent): ReleasesListAdapter.ItemListener {
            return object: ReleasesListAdapter.ItemListener {
                override fun itemClicked(item: NyaaReleasePreview) {
                    NyaaReleaseActivity.startNyaaReleaseActivity(item, listParent.getCurrentActivity())
                }

                override fun downloadMagnet(item: NyaaReleasePreview) {
                    AppUtils.openMagnetLink(item, listParent.getSnackBarParentView(),
                        listParent.getSnackBarAnchorView())
                }

                override fun downloadTorrent(item: NyaaReleasePreview) {
                    val newDownload = ReleaseId(item.number, item.dataSourceSpecs.source)
                    AppUtils.guardDownloadPermission(listParent.getCurrentActivity(),
                        listParent.storagePermissionRequestLauncher(), {
                        AppUtils.enqueueDownload(newDownload, listParent.getSnackBarParentView(),
                            listParent.getSnackBarAnchorView())
                    }, {
                        listParent.setQueuedDownload(newDownload)
                    })
                }
            }
        }

        fun setupStoragePermissionRequestLauncher(
            listParent: ReleaseListParent
        ): ActivityResultLauncher<String> {
            return listParent.createPermissionRequestLauncher { granted ->
                listParent.getQueuedDownload()?.let {
                    if (granted) {
                        AppUtils.enqueueDownload(it, listParent.getSnackBarParentView(),
                            listParent.getSnackBarAnchorView())
                    } else {
                        AppUtils.storagePermissionForDownloadDenied(listParent.getSnackBarParentView(),
                            listParent.getSnackBarAnchorView())
                    }
                    listParent.setQueuedDownload(null)
                }
            }
        }
    }
}