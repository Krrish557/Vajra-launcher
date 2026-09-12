package com.vajra.launcher.models

import androidx.annotation.DrawableRes

data class ToolDefinition(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val fullDescription: String,
    val environment: String = "debian",
    val executable: String,
    val state: InstallationState,
    @DrawableRes val iconRes: Int,
    val version: String? = null,
    val examples: List<String> = emptyList()
)
