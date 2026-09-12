package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.R
import com.vajra.launcher.data.config.PreferencesManager
import com.vajra.launcher.databinding.ScreenCustomizationBinding
import com.vajra.launcher.ui.adapters.CustomizationAdapter
import com.vajra.launcher.ui.adapters.CustomizationItem

class CustomizationViewController(
    container: ViewGroup,
    private val prefsManager: PreferencesManager,
    private val onThemeChanged: () -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenCustomizationBinding = ScreenCustomizationBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    init {
        val context = container.context
        binding.customBackBtn.setOnClickListener { onBack() }

        val items = listOf(
            CustomizationItem("theme", "Theme", "Dark / Light / AMOLED", R.drawable.ic_theme),
            CustomizationItem("layout", "Layout", "Dashboard, grid, navigation", R.drawable.ic_layout),
            CustomizationItem("accent", "Accent Colors", "Customize module colors", R.drawable.ic_palette),
            CustomizationItem("categories", "Visible Categories", "Select and reorder", R.drawable.ic_visibility),
            CustomizationItem("widgets", "Widgets", "Choose what to show", R.drawable.ic_widgets),
            CustomizationItem("haptics", "Haptics", "Feedback settings", R.drawable.ic_vibration),
            CustomizationItem("reset", "Reset to Default", "Restore original settings", R.drawable.ic_restore)
        )

        binding.customRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.customRecyclerView.adapter = CustomizationAdapter(items) { item ->
            when (item.id) {
                "theme" -> showThemeDialog(context)
                "accent" -> showAccentDialog(context)
                "reset" -> showResetDialog(context)
                "layout" -> Toast.makeText(context, "Layout: Cyber Workstation standard grid active", Toast.LENGTH_SHORT).show()
                "categories" -> Toast.makeText(context, "Categories: All 15 modules active", Toast.LENGTH_SHORT).show()
                "widgets" -> Toast.makeText(context, "Widgets: CPU, RAM, Battery, Temp active", Toast.LENGTH_SHORT).show()
                "haptics" -> Toast.makeText(context, "Haptics: Subtle touch feedback enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showThemeDialog(context: android.content.Context) {
        val themes = arrayOf("Dark (Default)", "AMOLED (Pure Black)", "Light")
        val currentThemeIndex = when (prefsManager.theme) {
            PreferencesManager.THEME_AMOLED -> 1
            PreferencesManager.THEME_LIGHT -> 2
            else -> 0
        }

        AlertDialog.Builder(context)
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedTheme = when (which) {
                    1 -> PreferencesManager.THEME_AMOLED
                    2 -> PreferencesManager.THEME_LIGHT
                    else -> PreferencesManager.THEME_DARK
                }
                prefsManager.theme = selectedTheme
                dialog.dismiss()
                onThemeChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAccentDialog(context: android.content.Context) {
        val accents = arrayOf("Cyber Blue (#4C9AFF)", "Matrix Cyan (#38D39F)")
        AlertDialog.Builder(context)
            .setTitle("Accent Colors")
            .setItems(accents) { _, which ->
                prefsManager.accentColor = if (which == 1) PreferencesManager.ACCENT_CYAN else PreferencesManager.ACCENT_BLUE
                Toast.makeText(context, "Accent updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showResetDialog(context: android.content.Context) {
        AlertDialog.Builder(context)
            .setTitle("Reset to Default")
            .setMessage("Restore original Vajra launcher settings?")
            .setPositiveButton("Reset") { _, _ ->
                prefsManager.resetToDefaults()
                Toast.makeText(context, "Settings restored to default", Toast.LENGTH_SHORT).show()
                onThemeChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
