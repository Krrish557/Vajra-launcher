package com.vajra.launcher.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vajra.launcher.databinding.ItemCategoryRowBinding
import com.vajra.launcher.models.ToolCategory

class CategoryAdapter(
    private val categories: List<ToolCategory>,
    private val onCategoryClick: (ToolCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val cat = categories[position]
        val context = holder.itemView.context

        holder.binding.categoryTitle.text = cat.title
        holder.binding.categorySubtitle.text = cat.subtitle
        holder.binding.categoryIcon.setImageResource(cat.iconRes)

        val accentColor = ContextCompat.getColor(context, cat.accentColorRes)
        holder.binding.categoryIcon.imageTintList = ColorStateList.valueOf(accentColor)

        holder.itemView.setOnClickListener {
            onCategoryClick(cat)
        }
    }

    override fun getItemCount(): Int = categories.size
}
