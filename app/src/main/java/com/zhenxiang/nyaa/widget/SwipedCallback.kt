package com.zhenxiang.nyaa.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.R

open class SwipedCallback(context: Context, swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
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
        listener?.onDeleteItem(viewHolder.bindingAdapterPosition)
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return super.getSwipeDirs(recyclerView, viewHolder)
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