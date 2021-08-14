package com.zhenxiang.nyaa.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.api.ReleaseCategory

class CategoryAutoCompleteTextView: MaterialAutoCompleteTextView {
    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)


    override fun convertSelectionToString(selectedItem: Any?): CharSequence {
        return if (selectedItem is ReleaseCategory) {
            AppUtils.getReleaseCategoryString(context, selectedItem)
        } else {
            super.convertSelectionToString(selectedItem)
        }
    }
}