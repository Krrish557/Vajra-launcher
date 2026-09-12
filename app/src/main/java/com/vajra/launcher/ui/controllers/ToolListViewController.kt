package com.vajra.launcher.ui.controllers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.ScreenToolListBinding
import com.vajra.launcher.models.ToolDefinition
import com.vajra.launcher.ui.adapters.ToolAdapter

class ToolListViewController(
    container: ViewGroup,
    val categoryId: String,
    private val onToolSelected: (String) -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenToolListBinding = ScreenToolListBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private val allTools: List<ToolDefinition>
    private val toolAdapter: ToolAdapter

    init {
        val category = ToolRegistry.getCategory(categoryId)
        binding.toolListCategoryTitle.text = category?.title ?: categoryId.uppercase()
        binding.toolListCategorySubtitle.text = category?.subtitle?.uppercase() ?: "TOOLS"

        binding.toolListBackBtn.setOnClickListener {
            onBack()
        }

        binding.toolListFilterBtn.setOnClickListener {
            Toast.makeText(container.context, "Filter: showing all tools", Toast.LENGTH_SHORT).show()
        }

        allTools = ToolRegistry.getToolsForCategory(categoryId)
        toolAdapter = ToolAdapter(allTools) { tool ->
            onToolSelected(tool.id)
        }

        binding.toolsRecyclerView.layoutManager = LinearLayoutManager(container.context)
        binding.toolsRecyclerView.adapter = toolAdapter

        binding.toolListSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            allTools
        } else {
            allTools.filter {
                it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
            }
        }
        toolAdapter.updateList(filtered)
    }
}
