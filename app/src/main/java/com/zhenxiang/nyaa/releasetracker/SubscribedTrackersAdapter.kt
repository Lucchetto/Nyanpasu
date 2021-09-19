package com.zhenxiang.nyaa.releasetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.AppUtils
import com.zhenxiang.nyaa.R
import java.text.DateFormat
import java.util.*

class SubscribedTrackersAdapter: RecyclerView.Adapter<SubscribedTrackersAdapter.ViewHolder>() {

    var listener: ItemClickedListener? = null

    private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<SubscribedTracker>() {
        override fun areItemsTheSame(
            oldItem: SubscribedTracker,
            newItem: SubscribedTracker
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SubscribedTracker,
            newItem: SubscribedTracker
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ: AsyncListDiffer<SubscribedTracker> = AsyncListDiffer(this, DIFF_CALLBACK)

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tracker_title)
        private val subtitle = view.findViewById<TextView>(R.id.subtitle)
        private val category = view.findViewById<TextView>(R.id.tracker_category)
        private val sourceAndUsername = view.findViewById<TextView>(R.id.tracker_source_username)
        private val latestRelease = view.findViewById<TextView>(R.id.latest_release_date)
        private val newReleasesCounter = view.findViewById<TextView>(R.id.new_releases_counter)

        private var itemData: SubscribedTracker? = null

        init {
            view.setOnClickListener { _ ->
                listener?.let {
                    itemData?.let { tracker ->
                        it.itemClicked(tracker)
                    }
                }
            }
        }

        fun bind(tracker: SubscribedTracker) {
            itemData = tracker
            category.text = title.context.getString(R.string.release_category,
                AppUtils.getReleaseCategoryString(category.context, tracker.dataSourceSpecs.category))
            latestRelease.text = if (tracker.hasPreviousReleases) {
                latestRelease.context.getString(R.string.tracker_latest_release,
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(tracker.latestReleaseTimestamp * 1000))
                )
            } else {
                latestRelease.context.getString(R.string.tracker_no_releases_yet)
            }
            when {
                tracker.newReleasesCount < 1 -> {
                    newReleasesCounter.text = null
                    newReleasesCounter.visibility = View.GONE
                    TextViewCompat.setTextAppearance(title, R.style.Style_Nyaasi_ReleasePreviewTitle)
                }
                tracker.newReleasesCount < 100 -> {
                    newReleasesCounter.text = tracker.newReleasesCount.toString()
                    newReleasesCounter.visibility = View.VISIBLE
                    TextViewCompat.setTextAppearance(title, R.style.Style_Nyaasi_ReleasePreviewTitle_Bold)
                }
                tracker.newReleasesCount >= 100 -> {
                    newReleasesCounter.text = "99+"
                    newReleasesCounter.visibility = View.VISIBLE
                    TextViewCompat.setTextAppearance(title, R.style.Style_Nyaasi_ReleasePreviewTitle_Bold)
                }
            }

            val titleText: String?
            if (tracker.searchQuery != null) {
                // First line the query
                titleText = if (tracker.name == null) {
                    tracker.searchQuery
                } else {
                    title.context.getString(R.string.tracker_keywords, tracker.searchQuery)
                }
                sourceAndUsername.text = tracker.username?.let {
                    sourceAndUsername.context.getString(R.string.tracker_from_data_source_and_user,
                        tracker.dataSourceSpecs.source.url, it)
                } ?: run {
                    sourceAndUsername.context.getString(R.string.tracker_from_data_source,
                        tracker.dataSourceSpecs.source.url)
                }

            } else if (tracker.username != null) {
                // First line the username
                titleText = title.context.getString(R.string.tracker_all_releases_from_user, tracker.username)
                // Hide username because not already showing in title
                sourceAndUsername.text = sourceAndUsername.context.getString(R.string.tracker_from_data_source,
                    tracker.dataSourceSpecs.source.url)
            } else {
                // else condition should never happen
                titleText = null
                sourceAndUsername.text = null
            }

            // If tracker has a custom name, show the name as title
            // then move the titleText to subtitle
            if (tracker.name == null) {
                subtitle.visibility = View.GONE
                title.text = titleText
            } else {
                title.text = tracker.name
                subtitle.text = titleText
                subtitle.visibility = View.VISIBLE
            }
        }
    }

    fun setData(newItems: List<SubscribedTracker>) {
        differ.submitList(newItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.release_tracker_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        differ.currentList.getOrNull(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface ItemClickedListener {
        fun itemClicked(item: SubscribedTracker)
    }
}