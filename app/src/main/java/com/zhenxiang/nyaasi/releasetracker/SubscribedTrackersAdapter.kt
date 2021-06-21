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
        private val usernameOrTrackerTitle = view.findViewById<TextView>(R.id.username_or_tracker_title)
        private val username = view.findViewById<TextView>(R.id.username)

        fun bind(tracker: SubscribedTracker) {
            if (tracker.searchQuery != null) {
                usernameOrTrackerTitle.text = tracker.searchQuery
                tracker.username?.let {
                    username.text = it
                    username.visibility = View.VISIBLE
                } ?: run {
                    username.text = null
                    username.visibility = View.GONE
                }
            } else if (tracker.username != null) {
                usernameOrTrackerTitle.text = tracker.username
                username.text = null
                username.visibility = View.GONE
            } else {
                // else condition should never happen
                usernameOrTrackerTitle.text = null
                username.text = null
                username.visibility = View.GONE
            }
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