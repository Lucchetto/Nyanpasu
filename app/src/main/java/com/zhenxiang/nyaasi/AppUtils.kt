package com.zhenxiang.nyaasi

import android.R
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.zhenxiang.nyaasi.api.NyaaReleaseCategory
import com.zhenxiang.nyaasi.api.NyaaReleasePreviewItem

class AppUtils {
    companion object {
        fun openMagnetLink(context: Context, item: NyaaReleasePreviewItem, parentView: View, anchorView: View? = null) {
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

        fun getNyaaCategoriesSpinner(context: Context): ArrayAdapter<String> {
            return ArrayAdapter(
                context,
                R.layout.simple_list_item_1,
                NyaaReleaseCategory.values().map { context.getString(it.stringResId) }
            )
        }
    }
}