package com.zhenxiang.nyaa.view

import android.view.View
import android.widget.TextView

fun TextView.setTextOrGone(text: String?) {
    text?.let {
        this.text = it
        this.visibility = View.VISIBLE
    } ?: run {
        this.visibility = View.GONE
    }
}