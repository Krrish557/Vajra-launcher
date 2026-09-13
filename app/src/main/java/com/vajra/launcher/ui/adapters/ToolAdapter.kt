package com.vajra.launcher.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vajra.launcher.R
import com.vajra.launcher.data.tools.ToolStatusResolver
import com.vajra.launcher.databinding.ItemToolRowBinding
import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolDefinition

class ToolAdapter(
    private var tools: List<ToolDefinition>,
    private val toolStatusResolver: ToolStatusResolver? = null,
    private val onToolClick: (ToolDefinition) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    class ToolViewHolder(val binding: ItemToolRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        val context = holder.itemView.context

        holder.binding.toolName.text = tool.name
        holder.binding.toolDescription.text = tool.description
        holder.binding.toolIcon.setImageResource(tool.iconRes)

        // Status badge styling with dynamic status check
        val currentStatus = toolStatusResolver?.getCachedStatus(tool) ?: tool.state
        when (currentStatus) {
            InstallationState.INSTALLED -> {
                holder.binding.toolStatusBadge.text = currentStatus.label
                holder.binding.toolStatusBadge.setBackgroundResource(R.drawable.bg_badge_installed)
                holder.binding.toolStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
                )
            }
            InstallationState.NOT_INSTALLED -> {
                holder.binding.toolStatusBadge.text = currentStatus.label
                holder.binding.toolStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                holder.binding.toolStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_uninstalled_text)
                )
            }
            else -> {
                holder.binding.toolStatusBadge.text = currentStatus.label
                holder.binding.toolStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                holder.binding.toolStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_text_secondary)
                )
            }
        }

        holder.itemView.setOnClickListener {
            onToolClick(tool)
        }
    }

    override fun getItemCount(): Int = tools.size

    fun updateList(newTools: List<ToolDefinition>) {
        tools = newTools
        notifyDataSetChanged()
    }
}
