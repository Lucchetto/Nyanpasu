package com.zhenxiang.nyaa

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.zhenxiang.nyaa.AppUtils.Companion.createPermissionRequestLauncher
import com.zhenxiang.nyaa.api.ReleaseId
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.release.NyaaReleaseActivity

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

                override fun copyMagnet(item: NyaaReleasePreview) {
                    copyToClipboardShowSnackbar(item.name, item.magnet,
                        listParent.getSnackBarParentView()
                            .context.getString(R.string.magnet_link_copied), listParent)
                }

                override fun copyTorrent(item: NyaaReleasePreview) {
                    copyToClipboardShowSnackbar(item.name, AppUtils.getReleaseTorrentUrl(
                        item.getReleaseId(), AppUtils.getUseProxy(listParent.getSnackBarParentView().context)),
                        listParent.getSnackBarParentView()
                            .context.getString(R.string.torrent_link_copied), listParent)
                }

                override fun copyReleaseLink(item: NyaaReleasePreview) {
                    copyToClipboardShowSnackbar(item.name, AppUtils.getReleasePageUrl(
                        item.getReleaseId(), AppUtils.getUseProxy(listParent.getSnackBarParentView().context)),
                        listParent.getSnackBarParentView()
                            .context.getString(R.string.release_link_copied), listParent)
                }
            }
        }

        private fun copyToClipboardShowSnackbar(label: String, text: String, snackbarText: String, listParent: ReleaseListParent) {
            copyToClipboardShowSnackbar(label, text, snackbarText,
                listParent.getSnackBarParentView(), listParent.getSnackBarAnchorView())
        }

        fun copyToClipboardShowSnackbar(label: String, text: String, snackbarText: String, parentView: View, anchorView: View?) {
            val clipData = ClipData.newPlainText(label, text)
            (parentView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
            val snack = Snackbar.make(parentView, snackbarText, Snackbar.LENGTH_SHORT)
            anchorView?.let {
                snack.anchorView = it
            }
            snack.show()
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