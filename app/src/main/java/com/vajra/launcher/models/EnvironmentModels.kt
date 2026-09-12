package com.vajra.launcher.models

enum class EnvironmentType(val label: String) {
    ANDROID("Android"),
    TERMUX("Termux"),
    DEBIAN("Debian"),
    LINUX("Linux"),
    EXTERNAL_APP("External App"),
    UNKNOWN("Unknown")
}

enum class EnvironmentStatus(val label: String) {
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    NOT_INSTALLED("Not installed"),
    NOT_CONFIGURED("Not configured"),
    UNKNOWN("Unknown"),
    ERROR("Error")
}

data class EnvironmentInfo(
    val type: EnvironmentType,
    val name: String,
    val isAvailable: Boolean,
    val status: EnvironmentStatus,
    val version: String? = null,
    val architecture: String? = null,
    val description: String,
    val packageName: String? = null
)

sealed class TermuxLaunchResult {
    object Success : TermuxLaunchResult()
    object NotInstalled : TermuxLaunchResult()
    data class Failed(val reason: String) : TermuxLaunchResult()
}
