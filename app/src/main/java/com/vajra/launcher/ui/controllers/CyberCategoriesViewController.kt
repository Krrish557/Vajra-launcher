package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.ScreenCyberCategoriesBinding
import com.vajra.launcher.ui.adapters.CategoryAdapter

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.vajra.launcher.R
import com.vajra.launcher.data.environment.EnvironmentManager
import com.vajra.launcher.models.TermuxLaunchResult

class CyberCategoriesViewController(
    container: ViewGroup,
    private val environmentManager: EnvironmentManager = EnvironmentManager(container.context),
    private val onCategorySelected: (String) -> Unit
) {
    val binding: ScreenCyberCategoriesBinding = ScreenCyberCategoriesBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    init {
        val context = container.context
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(context)

        val adapter = CategoryAdapter(ToolRegistry.categories) { category ->
            onCategorySelected(category.id)
        }
        binding.categoriesRecyclerView.adapter = adapter

        // Setup Environment Status Banner
        val termuxInfo = environmentManager.getTermuxInfo()
        if (termuxInfo.isAvailable) {
            binding.cyberEnvironmentDot.text = "●"
            binding.cyberEnvironmentDot.setTextColor(ContextCompat.getColor(context, R.color.vajra_accent_cyan))
            val vStr = if (termuxInfo.version != null) " (v${termuxInfo.version})" else ""
            binding.cyberEnvironmentText.text = "TERMUX READY$vStr"
            binding.cyberEnvironmentAction.text = "OPEN TERMUX ›"
            binding.cyberEnvironmentAction.setTextColor(ContextCompat.getColor(context, R.color.vajra_accent_cyan))
            binding.cyberEnvironmentBanner.setOnClickListener {
                when (val result = environmentManager.launchTermux(context)) {
                    is TermuxLaunchResult.Success -> {
                        // Launched successfully
                    }
                    is TermuxLaunchResult.NotInstalled -> {
                        Toast.makeText(context, "Termux is not installed.", Toast.LENGTH_SHORT).show()
                    }
                    is TermuxLaunchResult.Failed -> {
                        Toast.makeText(context, result.reason, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            binding.cyberEnvironmentDot.text = "○"
            binding.cyberEnvironmentDot.setTextColor(ContextCompat.getColor(context, R.color.vajra_text_tertiary))
            binding.cyberEnvironmentText.text = "TERMUX NOT INSTALLED"
            binding.cyberEnvironmentAction.text = "GUIDE ›"
            binding.cyberEnvironmentAction.setTextColor(ContextCompat.getColor(context, R.color.vajra_text_secondary))
            binding.cyberEnvironmentBanner.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Termux Environment Guide")
                    .setMessage("Termux provides the terminal emulator and Linux package subsystem for Vajra.\n\nTo enable command execution and tactical tools, install Termux from F-Droid or GitHub (com.termux).\n\nVajra does not install or modify packages automatically.")
                    .setPositiveButton("Close", null)
                    .show()
            }
        }
    }
}
