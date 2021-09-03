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

class SwipedCallback(context: Context, swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    : ItemTouchHelper.SimpleCallback(0, swipeDirs
) {

    var listener: ItemDeleteListener? = null
    private val deleteIcon: Drawable
    init {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        @ColorInt val color = typedValue.data
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_outline_delete_24)!!
        deleteIcon.setTint(color)

    }
    private val background = ColorDrawable(ContextCompat.getColor(context, R.color.design_default_color_error))

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Shouldn't happen but if item is deleted dismiss the popup
        if (viewHolder is ReleasesListAdapter.DownloadItemViewHolder) {
            viewHolder.popupMenu?.dismiss()
        }
        listener?.onDeleteItem(viewHolder.bindingAdapterPosition)
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

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView


        val iconMargin: Int = (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconTop: Int = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconBottom: Int = iconTop + deleteIcon.intrinsicHeight

        if (dX > 0) { // Swiping to the right
            val iconLeft = itemView.left + iconMargin
            val iconRight: Int = itemView.left + iconMargin + deleteIcon.getIntrinsicWidth()
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            background.setBounds(
                itemView.left, itemView.top,
                itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
            )
        } else if (dX < 0) { // Swiping to the left
            val iconLeft: Int = itemView.right - iconMargin - deleteIcon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            background.setBounds(
                itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom
            )
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        deleteIcon.draw(c)
    }

    interface ItemDeleteListener {
        // adapter position
        fun onDeleteItem(position: Int)
    }
}