package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.ScreenCyberCategoriesBinding
import com.vajra.launcher.ui.adapters.CategoryAdapter

class CyberCategoriesViewController(
    container: ViewGroup,
    private val onCategorySelected: (String) -> Unit
) {
    val binding: ScreenCyberCategoriesBinding = ScreenCyberCategoriesBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    init {
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(container.context)

        val adapter = CategoryAdapter(ToolRegistry.categories) { category ->
            onCategorySelected(category.id)
        }
        binding.categoriesRecyclerView.adapter = adapter
    }
}
