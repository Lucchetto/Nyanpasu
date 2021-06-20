package com.zhenxiang.nyaasi

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.db.NyaaReleasePreview
import java.text.DateFormat
import java.util.*
import androidx.annotation.ColorInt

import android.util.TypedValue
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil


class ReleasesListAdapter(private val showActions: Boolean = true): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_DOWNLOAD_ITEM = 0
    private val TYPE_FOOTER_ITEM = 1

    private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<NyaaReleasePreview>() {
        override fun areItemsTheSame(
            oldItem: NyaaReleasePreview,
            newItem: NyaaReleasePreview
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NyaaReleasePreview,
            newItem: NyaaReleasePreview
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ: AsyncListDiffer<NyaaReleasePreview> = AsyncListDiffer(this, DIFF_CALLBACK)

    var listener: ItemClickedListener? = null

    private var showFooter = true

    inner class DownloadItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //private val id: TextView = view.findViewById(R.id.release_id)
        private val title: TextView = view.findViewById(R.id.release_title)
        private val releaseDate: TextView = view.findViewById(R.id.release_date)
        private val magnetBtn: View = view.findViewById(R.id.magnet_btn)

        private var itemData: NyaaReleasePreview? = null

        init {
            if (!showActions) {
                view.findViewById<View>(R.id.magnet_btn).visibility = View.GONE
                view.findViewById<View>(R.id.download_btn).visibility = View.GONE
            }
            view.setOnClickListener {
                val itemData = itemData
                val listener = listener
                if (itemData != null && listener != null) {
                    listener.itemClicked(itemData)
                }
            }
            magnetBtn.setOnClickListener {
                val itemData = itemData
                val listener = listener
                if (itemData != null && listener != null) {
                    listener.downloadMagnet(itemData)
                }
            }
        }

        fun bind(item: NyaaReleasePreview) {
            itemData = item
            //id.text = item.id.toString()
            title.text = item.name
            releaseDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(
                Date(item.timestamp * 1000)
            )
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
            .inflate(if (isFooterItem) R.layout.loading_circle_footer else R.layout.release_preview_item, parent, false)
        return if (isFooterItem) FooterViewHolder(view) else DownloadItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DownloadItemViewHolder) {
            holder.bind(differ.currentList[position])
        } else if (holder is FooterViewHolder) {
            holder.setVisible(showFooter)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < itemCount - 1) TYPE_DOWNLOAD_ITEM else TYPE_FOOTER_ITEM
    }

    override fun getItemCount(): Int {
        // All data plus one for footer
        return differ.currentList.size + 1
    }

    fun setItems(newItems: List<NyaaReleasePreview>) {
        differ.submitList(newItems)
    }

    fun getItems(): List<NyaaReleasePreview> {
        return differ.currentList
    }

    interface ItemClickedListener {
        fun itemClicked(item: NyaaReleasePreview)
        fun downloadMagnet(item: NyaaReleasePreview)
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
        listener?.onDeleteItem(viewHolder.adapterPosition)
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