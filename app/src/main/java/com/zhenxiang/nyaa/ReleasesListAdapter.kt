package com.zhenxiang.nyaa

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.db.NyaaReleasePreview
import java.text.DateFormat
import java.util.*
import androidx.annotation.ColorInt

import android.util.TypedValue
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.zhenxiang.nyaa.db.NyaaReleasePreview.Companion.getReleaseId
import com.zhenxiang.nyaa.widget.SwipedCallback


class ReleasesListAdapter(private val showActions: Boolean = true): RecyclerView.Adapter<ReleasesListAdapter.DownloadItemViewHolder>() {

    private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<NyaaReleasePreview>() {
        override fun areItemsTheSame(
            oldItem: NyaaReleasePreview,
            newItem: NyaaReleasePreview
        ): Boolean {
            return oldItem.number == newItem.number
                    && oldItem.dataSourceSpecs.source == newItem.dataSourceSpecs.source
        }

        override fun areContentsTheSame(
            oldItem: NyaaReleasePreview,
            newItem: NyaaReleasePreview
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ: AsyncListDiffer<NyaaReleasePreview> = AsyncListDiffer(this, DIFF_CALLBACK)

    var listener: ItemListener? = null

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    inner class DownloadItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //private val id: TextView = view.findViewById(R.id.release_id)
        private val title: TextView = view.findViewById(R.id.release_title)
        private val releaseDate: TextView = view.findViewById(R.id.release_date)
        private val magnetBtn: View = view.findViewById(R.id.magnet_btn)
        private val downloadBtn: View = view.findViewById(R.id.download_btn)

        private var itemData: NyaaReleasePreview? = null
        var popupMenu: PopupMenu? = null
            private set

        init {
            if (!showActions) {
                magnetBtn.visibility = View.GONE
                downloadBtn.visibility = View.GONE
            } else {
                magnetBtn.setOnClickListener {
                    val itemData = itemData
                    val listener = listener
                    if (itemData != null && listener != null) {
                        listener.downloadMagnet(itemData)
                    }
                }

                magnetBtn.setOnLongClickListener {
                    val itemData = itemData
                    val listener = listener
                    if (itemData != null && listener != null) {
                        listener.copyMagnet(itemData)
                        true
                    } else {
                        false
                    }
                }
                downloadBtn.setOnLongClickListener {
                    val itemData = itemData
                    val listener = listener
                    if (itemData != null && listener != null) {
                        listener.copyTorrent(itemData)
                        true
                    } else {
                        false
                    }
                }

                downloadBtn.setOnClickListener {
                    val itemData = itemData
                    val listener = listener
                    if (itemData != null && listener != null) {
                        listener.downloadTorrent(itemData)
                    }
                }

                view.setOnLongClickListener {
                    showPopupMenu()
                    true
                }
            }

            view.setOnClickListener {
                val itemData = itemData
                val listener = listener
                if (itemData != null && listener != null) {
                    listener.itemClicked(itemData)
                }
            }
        }

        private fun showPopupMenu() {
            val itemData = itemData
            val listener = listener
            if (itemData != null && listener != null) {
                val popupMenu = PopupMenu(itemView.context, itemView)
                popupMenu.menuInflater.inflate(R.menu.release_preview_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.copy_magnet -> {
                            listener.copyMagnet(itemData)
                            true
                        }
                        R.id.copy_torrent -> {
                            listener.copyTorrent(itemData)
                            true
                        }
                        R.id.copy_release_link -> {
                            listener.copyReleaseLink(itemData)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
                popupMenu.setOnDismissListener {
                    // Clear the reference
                    this.popupMenu = null
                }

                this.popupMenu = popupMenu
            }
        }

        fun bind(item: NyaaReleasePreview) {
            itemData = item
            //id.text = item.id.toString()
            title.text = item.name
            releaseDate.text = releaseDate.context.getString(R.string.release_date,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(item.timestamp * 1000)))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.release_preview_item, parent, false)
        return DownloadItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setItems(newItems: List<NyaaReleasePreview>) {
        differ.submitList(newItems)
    }

    fun getItems(): List<NyaaReleasePreview> {
        return differ.currentList
    }

    interface ItemListener {
        fun itemClicked(item: NyaaReleasePreview)
        fun copyMagnet(item: NyaaReleasePreview)
        fun downloadMagnet(item: NyaaReleasePreview)
        fun copyTorrent(item: NyaaReleasePreview)
        fun downloadTorrent(item: NyaaReleasePreview)
        fun copyReleaseLink(item: NyaaReleasePreview)
    }
}

class ReleaseSwipedCallback(context: Context, swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    : SwipedCallback(context, swipeDirs
) {
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Shouldn't happen but if item is deleted dismiss the popup
        if (viewHolder is ReleasesListAdapter.DownloadItemViewHolder) {
            viewHolder.popupMenu?.dismiss()
        }
        super.onSwiped(viewHolder, direction)
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Prevent swiping with popup open since it would be annoying
        return if (viewHolder is ReleasesListAdapter.DownloadItemViewHolder && viewHolder.popupMenu != null) {
            0
        } else {
            super.getSwipeDirs(recyclerView, viewHolder)
        }
    }
}