package com.vajra.launcher.models

enum class InstallationState(val label: String) {
    INSTALLED("Installed"),
    NOT_INSTALLED("Not installed"),
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    INCOMPATIBLE("Incompatible"),
    ERROR("Error"),
    UNKNOWN("Unknown")
}
