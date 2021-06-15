package com.zhenxiang.nyaasi.releasetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.R

class SubscribedTrackersAdapter(): RecyclerView.Adapter<SubscribedTrackersAdapter.ViewHolder>() {

    val users = mutableListOf<SubscribedTracker>()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val username = view.findViewById<TextView>(R.id.username)

        fun bind(tracker: SubscribedTracker) {
            username.text = tracker.username
        }
    }

    fun setData(newData: List<SubscribedTracker>) {
        users.clear()
        users.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subscribed_user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        users.getOrNull(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}