package com.zhenxiang.nyaasi.releasetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.R
import com.zhenxiang.nyaasi.db.NyaaReleasePreview

class SubscribedUsersAdapter(): RecyclerView.Adapter<SubscribedUsersAdapter.ViewHolder>() {

    val users = mutableListOf<SubscribedUser>()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val username = view.findViewById<TextView>(R.id.username)

        fun bind(user: SubscribedUser) {
            username.text = user.username
        }
    }

    fun setData(newData: List<SubscribedUser>) {
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