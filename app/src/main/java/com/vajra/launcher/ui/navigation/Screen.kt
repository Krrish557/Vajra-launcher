package com.vajra.launcher.ui.navigation

sealed class Screen {
    object Home : Screen()
    object CyberCategories : Screen()
    data class ToolList(val categoryId: String) : Screen()
    data class ToolDetails(val toolId: String) : Screen()
    data class ToolConfig(val toolId: String) : Screen()
    object Apps : Screen()
    data class GlobalSearch(val query: String? = null) : Screen()
    object Customization : Screen()
    object SystemInfo : Screen()
}
