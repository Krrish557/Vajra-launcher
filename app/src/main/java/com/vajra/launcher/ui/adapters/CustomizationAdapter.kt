package com.vajra.launcher.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.vajra.launcher.databinding.ItemCustomizationRowBinding

data class CustomizationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
)

class CustomizationAdapter(
    private val items: List<CustomizationItem>,
    private val onItemClick: (CustomizationItem) -> Unit
) : RecyclerView.Adapter<CustomizationAdapter.CustomViewHolder>() {

    class CustomViewHolder(val binding: ItemCustomizationRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemCustomizationRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = items[position]

        holder.binding.customRowTitle.text = item.title
        holder.binding.customRowSubtitle.text = item.subtitle
        holder.binding.customRowIcon.setImageResource(item.iconRes)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
