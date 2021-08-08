package com.zhenxiang.nyaasi.releasetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.R
import java.text.DateFormat
import java.util.*

class SubscribedTrackersAdapter(): RecyclerView.Adapter<SubscribedTrackersAdapter.ViewHolder>() {

    val users = mutableListOf<SubscribedTracker>()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tracker_title)
        private val category = view.findViewById<TextView>(R.id.tracker_category)
        private val username = view.findViewById<TextView>(R.id.tracker_username)
        private val latestRelease = view.findViewById<TextView>(R.id.latest_release_date)

        fun bind(tracker: SubscribedTracker) {
            category.text = category.context.getString(R.string.release_category,
                category.context.getString(tracker.category.stringResId)
            )
            latestRelease.text = latestRelease.context.getString(R.string.tracker_latest_release,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.lastReleaseTimestamp * 1000))
            )

            if (tracker.searchQuery != null) {
                // First line the query
                title.text = tracker.searchQuery
                // Second line is username is present
                tracker.username?.let {
                    username.text = username.context.getString(R.string.tracker_from_user, it)
                    username.visibility = View.VISIBLE
                } ?: run {
                    // Hide username because not available
                    username.text = null
                    username.visibility = View.GONE
                }
            } else if (tracker.username != null) {
                // First line the username
                title.text = title.context.getString(R.string.tracker_all_releases_from_user, tracker.username)
                // Hide username because not already showing in title
                username.text = null
                username.visibility = View.GONE
            } else {
                // else condition should never happen
                title.text = null
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
            .inflate(R.layout.release_tracker_item, parent, false)
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