package com.zhenxiang.nyaa.releasetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.R
import com.zhenxiang.nyaa.view.setTextOrGone

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
        private val subtitle = view.findViewById<TextView>(R.id.tracker_subtitle)
        private val categoryAndDataSource = view.findViewById<TextView>(R.id.tracker_category)
        private val username = view.findViewById<TextView>(R.id.tracker_source_username)
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
            val formattedTexts = SubscribedTrackerFormattedTexts.fromTracker(tracker, title.context)

            title.setTextOrGone(formattedTexts.title)
            subtitle.setTextOrGone(formattedTexts.subtitle)
            categoryAndDataSource.setTextOrGone(formattedTexts.categoryAndDataSource)
            username.setTextOrGone(formattedTexts.username)
            latestRelease.setTextOrGone(formattedTexts.latestRelease)
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