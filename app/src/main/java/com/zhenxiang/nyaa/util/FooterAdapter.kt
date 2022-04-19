package com.zhenxiang.nyaa.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.R

class FooterAdapter: RecyclerView.Adapter<FooterAdapter.ViewHolder>() {

    var showLoading = false
    @SuppressLint("NotifyDataSetChanged")
    set(value) {
        if (value != field) {
            field = value
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.loading_circle_footer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return if (showLoading) 1 else 0
    }
}