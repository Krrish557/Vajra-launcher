package com.vajra.launcher.models

import androidx.annotation.DrawableRes

data class ToolDefinition(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val fullDescription: String,
    val environment: ToolEnvironment = ToolEnvironment.DEBIAN,
    val executable: String,
    val state: InstallationState,
    @DrawableRes val iconRes: Int,
    val version: String? = null,
    val quickActions: List<ToolAction> = emptyList(),
    val advancedOptions: List<ToolConfigOption> = emptyList(),
    val documentation: ToolDocumentation? = null,
    val examples: List<String> = emptyList(),
    val requiresRoot: Boolean = false,
    val requiresNetwork: Boolean = false
) {
    /**
     * Declared baseline state in the tool catalog.
     * Decoupled from dynamic runtime detection (Phase 18.7).
     */
    val declaredState: InstallationState
        get() = state
}
