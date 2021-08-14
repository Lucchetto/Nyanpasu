package com.zhenxiang.nyaa.api

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.zhenxiang.nyaa.AppUtils

class CategoriesAdapter: ArrayAdapter<ReleaseCategory> {

    constructor(context: Context, @LayoutRes resource: Int, objects: Array<ReleaseCategory>):
            super(context, resource, 0, listOf(*objects)) {
            }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val item: ReleaseCategory? = getItem(position)
        item?.let {
            (view as TextView).text = AppUtils.getReleaseCategoryString(view.context, it)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val item: ReleaseCategory? = getItem(position)
        item?.let {
            (view as TextView).text = AppUtils.getReleaseCategoryString(view.context, it)
        }
        return view
    }

    override fun getFilter(): Filter {
        return super.getFilter()
    }
}