package com.vajra.launcher.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vajra.launcher.databinding.ItemSearchRowBinding
import com.vajra.launcher.models.SearchResultItem

class SearchAdapter(
    private var results: List<SearchResultItem>,
    private val onResultClick: (SearchResultItem) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(val binding: ItemSearchRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = results[position]

        holder.binding.searchResultTitle.text = item.title
        holder.binding.searchResultSubtitle.text = item.subtitle
        holder.binding.searchResultIcon.setImageResource(item.iconRes)

        holder.itemView.setOnClickListener {
            onResultClick(item)
        }
    }

    override fun getItemCount(): Int = results.size

    fun updateResults(newResults: List<SearchResultItem>) {
        results = newResults
        notifyDataSetChanged()
    }
}
