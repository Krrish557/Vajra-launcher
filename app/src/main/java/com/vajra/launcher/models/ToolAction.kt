package com.vajra.launcher.models

data class ToolAction(
    val id: String,
    val label: String,
    val commandTemplate: String,
    val description: String = ""
)

data class ToolConfigOption(
    val id: String,
    val label: String,
    val flag: String,
    val defaultValue: Boolean = false,
    val description: String = ""
)

data class ToolDocumentation(
    val manPage: String? = null,
    val docsUrl: String? = null,
    val syntax: String? = null
)
