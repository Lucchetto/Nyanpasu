package com.zhenxiang.nyaasi

import android.content.Context
import android.view.MotionEvent

import androidx.recyclerview.widget.RecyclerView

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener


open class RecyclerViewItemClickListener(context: Context) : OnItemTouchListener {

    private val mGestureDetector: GestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView: View? = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            onItemClick(childView, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    open fun onItemClick(view: View, position: Int) {
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}
