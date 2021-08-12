package com.zhenxiang.nyaa.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.NyaaReleaseCategory
import com.zhenxiang.nyaa.api.ReleaseCategory

class BrowsingSpecsSelectorView: LinearLayout {
    private val categorySpinner: TitledSpinner
    private val dataSourceSpinner: TitledSpinner

    var listener: OnSpecsChangedListener? = null

    constructor (context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.browsing_specs_selector_view, this, true)
        orientation = HORIZONTAL

        categorySpinner = findViewById(R.id.categories_selection)
        dataSourceSpinner = findViewById(R.id.data_source_selection)

        categorySpinner.spinner.adapter = AppUtils.getNyaaCategoriesSpinner(context)

        // Prevent listener from firing on start
        categorySpinner.spinner.setSelection(0, false)
        categorySpinner.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener?.releaseCategoryChanged(NyaaReleaseCategory.values()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        dataSourceSpinner.spinner.adapter = AppUtils.getDataSourcesSpinner(context)
        // Prevent listener from firing on start
        dataSourceSpinner.spinner.setSelection(0, false)
        dataSourceSpinner.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener?.dataSourceChanged(ApiDataSource.values()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    interface OnSpecsChangedListener {
        fun releaseCategoryChanged(releaseCategory: ReleaseCategory)

        fun dataSourceChanged(apiDataSource: ApiDataSource)
    }
}