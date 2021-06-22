package com.zhenxiang.nyaasi.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.R

class FooterAdapter: RecyclerView.Adapter<FooterAdapter.ViewHolder>() {

    // Hax array with single item which indicates when loading show be visible
    // Default is true, so it's visible
    private val footer = booleanArrayOf(true)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val loadingCircle = view.findViewById<View>(R.id.footer_loading_circle)
        fun setVisible(visible: Boolean) {
            loadingCircle.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.loading_circle_footer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setVisible(footer[position])
    }

    override fun getItemCount(): Int {
        return footer.size
    }

    fun showLoading(loading: Boolean) {
        footer[0] = loading
    }
}