package com.zhenxiang.nyaa

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.zhenxiang.nyaa.api.*
import com.zhenxiang.nyaa.db.NyaaReleasePreview

class AppUtils {
    companion object {
        fun ActivityResultCaller.createPermissionRequestLauncher(success: (isGranted: Boolean) -> Unit): ActivityResultLauncher<String> {
            return registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                success(isGranted)
            }
        }

        fun openMagnetLink(item: NyaaReleasePreview, parentView: View, anchorView: View? = null) {
            try {
                parentView.context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(item.magnet))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
                val snack = Snackbar.make(parentView, R.string.no_magnet_supported_app_installed, Snackbar.LENGTH_SHORT)
                anchorView?.let {
                    snack.anchorView = it
                }
                snack.show()
            }
        }

        fun guardDownloadPermission(context: Context, permissionRequestLauncher: ActivityResultLauncher<String>,
                                    granted: () -> Unit, requesting: () -> Unit) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        granted()
                    }
                    else -> {
                        requesting()
                        permissionRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            } else {
                granted()
            }
        }

        fun storagePermissionForDownloadDenied(parentView: View, anchorView: View? = null) {
            val snackbar = Snackbar.make(parentView, R.string.permission_denied_hint, Snackbar.LENGTH_SHORT)
            anchorView?.let {
                snackbar.anchorView = it
            }
            snackbar.setAction(R.string.grant_permission) { _ ->
                parentView.context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${parentView.context.packageName}")
                    )
                )
            }
            snackbar.show()
        }

        fun getUseProxy(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.use_proxy_key), false)
        }

        fun getReleaseTorrentUrl(releaseId: ReleaseId, useProxy: Boolean): String {
            return "http://${NyaaPageProvider.getProperUrl(releaseId.dataSource, useProxy)}/download/${releaseId.number}.torrent"
        }

        fun getReleasePageUrl(releaseId: ReleaseId, useProxy: Boolean): String {
            return "https://${NyaaPageProvider.getProperUrl(releaseId.dataSource, useProxy)}/view/${releaseId.number}"
        }

        fun enqueueDownload(releaseId: ReleaseId, parentView: View, anchorView: View? = null): Long? {
            return try {
                val manager = parentView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(getReleaseTorrentUrl(releaseId, getUseProxy(parentView.context)))
                val request = DownloadManager.Request(uri)
                request.setVisibleInDownloadsUi(true)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
                manager.enqueue(request)
            } catch (e: Exception) {
                val snack = Snackbar.make(parentView, R.string.cannot_download_release_hint, Snackbar.LENGTH_SHORT)
                anchorView?.let {
                    snack.anchorView = it
                }
                snack.show()
                null
            }
        }

        fun getDataSourcesArrayFormatted() = ApiDataSource.values().map { it.url }

        fun getReleaseCategoryString(context: Context, releaseCategory: ReleaseCategory): String {
            return context.getString(
                releaseCategory.getStringResId()
            )
        }

        fun getCategoriesAdapter(context: Context, dataSource: ApiDataSource, forSpinner: Boolean): ArrayAdapter<ReleaseCategory> {
            val adapter = CategoriesAdapter(
                context,
                if (forSpinner) R.layout.spinner_item else R.layout.spinner_dropdown_item,
                dataSource.categories
            )
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            return adapter
        }

        fun getDataSourcesAdapter(context: Context, forSpinner: Boolean): ArrayAdapter<String> {
            val adapter = ArrayAdapter(
                context,
                if (forSpinner) R.layout.spinner_item else R.layout.spinner_dropdown_item,
                getDataSourcesArrayFormatted()
            )
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            return adapter
        }
    }
}