package com.vajra.launcher.models

import androidx.annotation.DrawableRes

enum class SearchType(val label: String) {
    ALL("All"),
    TOOL("Tools"),
    CATEGORY("Category"),
    APP("Apps"),
    COMMAND("Commands"),
    DOC("Docs")
}

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val type: SearchType,
    @DrawableRes val iconRes: Int,
    val payloadId: String
)
