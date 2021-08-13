package com.zhenxiang.nyaa.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.api.ApiDataSource
import com.zhenxiang.nyaa.api.ReleaseCategory
import com.zhenxiang.nyaa.widget.QuietSpinner

class BrowsingSpecsSelectorView: LinearLayout {
    private val categorySpinner: QuietSpinner
    private val dataSourceSpinner: QuietSpinner

    var listener: OnSpecsChangedListener? = null
    private var categories = ApiDataSource.NYAA_SI.categories
    var selectedCategory = categories[0]
        private set

    constructor (context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.browsing_specs_selector_view, this, true)
        orientation = HORIZONTAL

        categorySpinner = findViewById(R.id.categories_selection)
        findViewById<View>(R.id.categories_selection_container).setOnClickListener {
            categorySpinner.performClick()
        }
        dataSourceSpinner = findViewById(R.id.data_source_selection)
        findViewById<View>(R.id.data_source_selection_container).setOnClickListener {
            dataSourceSpinner.performClick()
        }

        setupDataSources()
    }

    private fun setupDataSources() {
        categorySpinner.adapter = AppUtils.getCategoriesSpinner(context, ApiDataSource.NYAA_SI)
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
                listener?.releaseCategoryChanged(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        dataSourceSpinner.adapter = AppUtils.getDataSourcesSpinner(context)
        dataSourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newDataSource = ApiDataSource.values()[position]
                categorySpinner.adapter = AppUtils.getCategoriesSpinner(context, newDataSource)
                categories = newDataSource.categories
                listener?.dataSourceChanged(newDataSource)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    fun selectDataSource(index: Int) {
        dataSourceSpinner.setSelection(index, false)
        val newDataSource = ApiDataSource.values()[index]
        categorySpinner.adapter = AppUtils.getCategoriesSpinner(context, newDataSource)
        categories = newDataSource.categories
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchRestoreInstanceState(container)
    }

    interface OnSpecsChangedListener {
        fun releaseCategoryChanged(releaseCategory: ReleaseCategory)

        fun dataSourceChanged(apiDataSource: ApiDataSource)
    }
}