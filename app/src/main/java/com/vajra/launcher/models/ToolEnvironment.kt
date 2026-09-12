package com.vajra.launcher.models

enum class ToolEnvironment(val label: String) {
    TERMUX("Termux"),
    DEBIAN("Debian"),
    LINUX("Linux"),
    ANDROID("Android"),
    EXTERNAL_APP("External App"),
    UNKNOWN("Unknown")
}
