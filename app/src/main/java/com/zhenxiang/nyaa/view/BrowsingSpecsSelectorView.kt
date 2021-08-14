package com.zhenxiang.nyaa.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
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

    private val dataSourceListener: AdapterView.OnItemSelectedListener
    private val categoryListener: AdapterView.OnItemSelectedListener

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

        categoryListener = object : AdapterView.OnItemSelectedListener {
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

        dataSourceListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newDataSource = ApiDataSource.values()[position]
                updateCategories(newDataSource)
                listener?.dataSourceChanged(newDataSource)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        setupDataSources()
    }

    private fun updateCategories(dataSource: ApiDataSource) {
        categorySpinner.adapter = AppUtils.getCategoriesAdapter(context, dataSource, true)
        categories = dataSource.categories
    }

    private fun setupDataSources() {
        //categorySpinner.adapter = AppUtils.getCategoriesSpinner(context, ApiDataSource.NYAA_SI)
        categorySpinner.onItemSelectedListener = categoryListener

        dataSourceSpinner.onItemSelectedListener = dataSourceListener
    }

    fun selectDataSource(index: Int) {
        // Hax to avoid firing the category listener twice
        dataSourceSpinner.onItemSelectedListener = null
        if (dataSourceSpinner.adapter == null) {
            dataSourceSpinner.adapter = AppUtils.getDataSourcesAdapter(context, true)
        }
        dataSourceSpinner.setSelection(index, false)
        val newDataSource = ApiDataSource.values()[index]
        updateCategories(newDataSource)
        // Hax to avoid firing the category listener twice
        dataSourceSpinner.onItemSelectedListener = dataSourceListener
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        superState?.let {
            val ss = SavedState(superState)
            ss.selectedCategoryIndex = categorySpinner.selectedItemPosition
            ss.selectedDataSourceIndex = dataSourceSpinner.selectedItemPosition
            return ss
        }
        return superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        // Hax to avoid firing the listener
        dataSourceSpinner.onItemSelectedListener = null
        categorySpinner.onItemSelectedListener = null
        super.onRestoreInstanceState(state)
        dataSourceSpinner.adapter = AppUtils.getDataSourcesAdapter(context, true)
        if (state != null && state is SavedState) {
            dataSourceSpinner.setSelection(state.selectedDataSourceIndex, false)
            updateCategories(ApiDataSource.values()[state.selectedDataSourceIndex])
            categorySpinner.setSelection(state.selectedCategoryIndex, false)
        }
        // Hax to avoid firing the listener
        dataSourceSpinner.onItemSelectedListener = dataSourceListener
        categorySpinner.onItemSelectedListener = categoryListener
    }

    internal class SavedState: BaseSavedState {
        var selectedCategoryIndex = 0
        var selectedDataSourceIndex = 0

        constructor(superState: Parcelable): super(superState)

        constructor(parcel: Parcel): super(parcel) {
            selectedCategoryIndex = parcel.readInt()
            selectedDataSourceIndex = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedCategoryIndex)
            out.writeInt(selectedDataSourceIndex)
        }

        @JvmField val CREATOR: Creator<SavedState> =
            object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }

    interface OnSpecsChangedListener {
        fun releaseCategoryChanged(releaseCategory: ReleaseCategory)

        fun dataSourceChanged(apiDataSource: ApiDataSource)
    }
}