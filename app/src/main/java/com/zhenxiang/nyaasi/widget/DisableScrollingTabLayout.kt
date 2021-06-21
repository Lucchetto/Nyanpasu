package com.zhenxiang.nyaasi.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.annotation.Nullable
import com.google.android.material.R
import com.google.android.material.tabs.TabLayout
import android.view.ViewGroup




class DisableScrollingTabLayout: TabLayout {

    var scrolling = true
    set(value) {
        field = value
        isFocusable = value
        descendantFocusability = if (value) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
    }

    constructor (context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, R.attr.tabStyle)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (scrolling) super.dispatchTouchEvent(ev) else true
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (scrolling) super.dispatchKeyEvent(event) else true
    }
}