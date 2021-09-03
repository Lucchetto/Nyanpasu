package com.zhenxiang.nyaa.db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaa.R

class SearchHistoryAdapter: RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    private var suggestions = emptyList<NyaaSearchHistoryItem>()
    var listener: OnSuggestionActionListener? = null

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener { _ ->
                listener?.let {
                    suggestions.getOrNull(adapterPosition)?.let { suggestion ->
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
        holder.bind(suggestions[position])
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    fun updateList(newList: List<NyaaSearchHistoryItem>) {
        suggestions = newList
        notifyDataSetChanged()
    }

    interface OnSuggestionActionListener {
        fun onSuggestionSelected(suggestion: NyaaSearchHistoryItem)
    }
}