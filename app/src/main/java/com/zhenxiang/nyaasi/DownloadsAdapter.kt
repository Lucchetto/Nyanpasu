package com.zhenxiang.nyaasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.api.NyaaDownloadItem
import java.text.DateFormat

class DownloadsAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_DOWNLOAD_ITEM = 0
    private val TYPE_FOOTER_ITEM = 1

    private val items = mutableListOf<NyaaDownloadItem>()

    private var showFooter = true

    class DownloadItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //private val id: TextView = view.findViewById(R.id.release_id)
        private val title: TextView = view.findViewById(R.id.release_title)
        private val releaseDate: TextView = view.findViewById(R.id.release_date)

        fun bind(item: NyaaDownloadItem) {
            //id.text = item.id.toString()
            title.text = item.name
            releaseDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(item.date)
        }
    }

    class FooterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val loadingCircle = view.findViewById<View>(R.id.footer_loading_circle)
        fun setVisible(visible: Boolean) {
            loadingCircle.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun setFooterVisible(visible: Boolean) {
        if (visible != showFooter) {
            showFooter = visible
            // Update last item which is footer
            notifyItemChanged(itemCount - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val isFooterItem = viewType == TYPE_FOOTER_ITEM
        val view = LayoutInflater.from(parent.context)
            .inflate(if (isFooterItem) R.layout.loading_circle_footer else R.layout.download_item, parent, false)
        return if (isFooterItem) FooterViewHolder(view) else DownloadItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DownloadItemViewHolder) {
            val element = items[position]
            holder.bind(element)
        } else if (holder is FooterViewHolder) {
            holder.setVisible(showFooter)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < itemCount - 1) TYPE_DOWNLOAD_ITEM else TYPE_FOOTER_ITEM
    }

    override fun getItemCount(): Int {
        // All data plus one for footer
        return items.size + 1
    }

    fun setItems(newItems: List<NyaaDownloadItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}