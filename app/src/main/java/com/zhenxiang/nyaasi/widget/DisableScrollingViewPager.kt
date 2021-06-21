package com.zhenxiang.nyaasi.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class DisableScrollingViewPager: ViewPager {

    var scrolling = true

    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (this.scrolling) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return if (this.scrolling) {
            super.onInterceptTouchEvent(event)
        } else false
    }

}