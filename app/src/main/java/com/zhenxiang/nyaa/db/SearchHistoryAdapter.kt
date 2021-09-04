package com.zhenxiang.nyaa.db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.R

class SearchHistoryAdapter: RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<NyaaSearchHistoryItem>() {
        override fun areItemsTheSame(
            oldItem: NyaaSearchHistoryItem,
            newItem: NyaaSearchHistoryItem
        ): Boolean {
            return oldItem.searchQuery == newItem.searchQuery
        }

        override fun areContentsTheSame(
            oldItem: NyaaSearchHistoryItem,
            newItem: NyaaSearchHistoryItem
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ: AsyncListDiffer<NyaaSearchHistoryItem> = AsyncListDiffer(this, DIFF_CALLBACK)

    var listener: OnSuggestionActionListener? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener { _ ->
                listener?.let {
                    differ.currentList.getOrNull(bindingAdapterPosition)?.let { suggestion ->
                        it.onSuggestionSelected(suggestion)
                    }
                }
            }
        }

        private val textView = view.findViewById<TextView>(R.id.suggestion_string)

        fun bind(suggestion: NyaaSearchHistoryItem) {
            textView.text = suggestion.searchQuery
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getItem(position: Int): NyaaSearchHistoryItem? {
        return differ.currentList.getOrNull(position)
    }

    fun updateList(newList: List<NyaaSearchHistoryItem>) {
        differ.submitList(newList)
    }

    interface OnSuggestionActionListener {
        fun onSuggestionSelected(suggestion: NyaaSearchHistoryItem)
    }
}