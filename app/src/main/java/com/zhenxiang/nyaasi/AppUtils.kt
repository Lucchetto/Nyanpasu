package com.zhenxiang.nyaasi

import android.R
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.db.NyaaReleasePreview

class AppUtils {
    companion object {
        fun openMagnetLink(context: Context, item: NyaaReleasePreview, parentView: View, anchorView: View? = null) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(item.magnet))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
                val snack = Snackbar.make(parentView, "No installed application can handle magnet links", Snackbar.LENGTH_SHORT)
                anchorView?.let {
                    snack.anchorView = it
                }
                snack.show()
            }
        }

        fun enqueueDownload(context: Context, releaseId: Int, parentView: View, anchorView: View? = null): Long? {
            return try {
                val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse("http://nyaa.si/download/$releaseId.torrent")
                val request = DownloadManager.Request(uri)
                request.setVisibleInDownloadsUi(true)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
                manager.enqueue(request)
            } catch (e: Exception) {
                val snack = Snackbar.make(parentView, "Can't download the selected release", Snackbar.LENGTH_SHORT)
                anchorView?.let {
                    snack.anchorView = it
                }
                snack.show()
                null
            }
        }

        fun getNyaaCategoriesSpinner(context: Context): ArrayAdapter<String> {
            return ArrayAdapter(
                context,
                R.layout.simple_list_item_1,
                NyaaReleaseCategory.values().map { context.getString(it.stringResId) }
            )
        }
    }
}