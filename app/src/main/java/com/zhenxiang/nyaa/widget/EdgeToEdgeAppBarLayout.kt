package com.zhenxiang.nyaa.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.zhenxiang.nyaa.R

class EdgeToEdgeAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.appBarLayoutStyle,
) : AppBarLayout(
    context, attrs, defStyleAttr
) {
    init {
        statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context).apply {
            elevation = resources.getDimension(R.dimen.design_appbar_elevation)
        }
    }
}