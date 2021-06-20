package com.zhenxiang.nyaasi.widget

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class ReleaseItemAnimator: DefaultItemAnimator() {

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        val parent = super.animateAdd(holder)
        // Hax away the default animation
        holder?.itemView?.alpha = 1f
        return parent
    }
}