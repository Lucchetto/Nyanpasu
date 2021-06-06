package com.zhenxiang.nyaasi.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import com.zhenxiang.nyaasi.R

class ReleaseDataItemView: LinearLayout {

    private val titleView: TextView
    private val valueView: TextView

    constructor (context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.release_data_item, this, true)
        orientation = VERTICAL

        titleView = findViewById(R.id.title)
        valueView = findViewById(R.id.value)

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ReleaseDataItemView, defStyleAttr, defStyleRes)

        titleView.text = a.getString(R.styleable.ReleaseDataItemView_title)
        valueView.text = a.getString(R.styleable.ReleaseDataItemView_value)

        a.recycle()
    }

    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setValue(value: String) {
        valueView.text = value
    }
}