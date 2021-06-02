package com.zhenxiang.nyaasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.api.NyaaDownloadItem

class DownloadsAdapter(): RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    val items = mutableListOf<NyaaDownloadItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.test_text)

        fun bind(item: NyaaDownloadItem) {
            textView.text = "${item.id} ${item.name} ${item.magnet} ${item.date.toLocaleString()}"
            textView.isSelected = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.download_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = items[position]
        holder.bind(element)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(newItems: List<NyaaDownloadItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}