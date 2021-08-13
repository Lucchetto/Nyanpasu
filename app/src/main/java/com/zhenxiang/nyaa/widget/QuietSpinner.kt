package com.zhenxiang.nyaa.widget

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner

// Variant of Spinner that shuts the fuck up when setting an adapter
// Doesn't call OnItemSelectedListener.onItemSelected basically
class QuietSpinner: AppCompatSpinner {
    constructor(context: Context): super(context)

    constructor(context: Context, mode: Int): super(context, mode)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, mode: Int): super(context, attrs, defStyleAttr, mode)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, mode: Int, popupTheme: Resources.Theme): super(context, attrs, defStyleAttr, mode, popupTheme)

    override fun setAdapter(adapter: SpinnerAdapter?) {
        super.setAdapter(adapter)
        setSelection(0, false)
    }
}