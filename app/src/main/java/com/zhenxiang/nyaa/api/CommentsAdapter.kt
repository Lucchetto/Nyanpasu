package com.zhenxiang.nyaa.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhenxiang.nyaa.R
import java.text.DateFormat
import java.util.*
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin


class CommentsAdapter: RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<ReleaseComment>() {
        override fun areItemsTheSame(
            oldItem: ReleaseComment,
            newItem: ReleaseComment
        ): Boolean {
            return oldItem.timestamp == newItem.timestamp
                    && oldItem.username == newItem.username
        }

        override fun areContentsTheSame(
            oldItem: ReleaseComment,
            newItem: ReleaseComment
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ: AsyncListDiffer<ReleaseComment> = AsyncListDiffer(this, DIFF_CALLBACK)

    inner class CommentViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val markwon = Markwon.builder(view.context)
            .usePlugin(LinkifyPlugin.create(true))
            .usePlugin(GlideImagesPlugin.create(view.context))
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .build()

        private val userImage = view.findViewById<ImageView>(R.id.comment_profile_picture)
        private val usernameAndDate = view.findViewById<TextView>(R.id.comment_username_and_date)
        private val content = view.findViewById<TextView>(R.id.comment_content)

        fun bind(comment: ReleaseComment) {
            Glide.with(userImage).load(comment.userImage)
                .placeholder(R.drawable.default_pic)
                .into(userImage)
            usernameAndDate.text = content.context.getString(R.string.comment_username_and_date,
                comment.username, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(
                    Date(comment.timestamp * 1000)
                )
            )
            markwon.setMarkdown(content, comment.content)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.release_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setList(newList: List<ReleaseComment>) {
        differ.submitList(newList)
    }
}