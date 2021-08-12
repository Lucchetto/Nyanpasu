package com.zhenxiang.nyaa.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.Nullable
import com.zhenxiang.nyaa.R

class TitledSpinner: LinearLayout {

    private val titleView: TextView
    val spinner: Spinner

    constructor (context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.TitledSpinner)

    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.titled_spinner, this, true)
        orientation = HORIZONTAL

        titleView = findViewById(R.id.title)
        spinner = findViewById(R.id.spinner)

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.TitledSpinner, defStyleAttr, defStyleRes)

        titleView.text = a.getString(R.styleable.TitledSpinner_spinnerTitle)
        titleView.setTextAppearance(context, a.getResourceId(R.styleable.TitledSpinner_titleTextAppearance, 0))

        a.recycle()

        setOnClickListener { _ ->
            spinner.performClick()
        }
    }
}