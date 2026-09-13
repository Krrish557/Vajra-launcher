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
    READY("Ready"),
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    NOT_INSTALLED("Not installed"),
    NOT_CONFIGURED("Not configured"),
    UNKNOWN("Unknown"),
    ERROR("Error")
}

enum class CommunicationStatus(val label: String) {
    READY("Ready"),
    NOT_CONFIGURED("Not configured"),
    NOT_INSTALLED("Not installed"),
    UNAVAILABLE("Unavailable"),
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
    val packageName: String? = null,
    val isReady: Boolean = (status == EnvironmentStatus.READY || status == EnvironmentStatus.AVAILABLE),
    val capabilities: List<String> = emptyList()
)

sealed class TermuxLaunchResult {
    object Success : TermuxLaunchResult()
    object NotInstalled : TermuxLaunchResult()
    data class Failed(val reason: String) : TermuxLaunchResult()
}

data class ToolExecutionRequest(
    val toolId: String,
    val actionId: String,
    val target: String,
    val options: Map<String, Any> = emptyMap()
)

data class ExecutionResult(
    val id: String,
    val toolId: String,
    val actionId: String,
    val target: String,
    val environment: EnvironmentType,
    val success: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

enum class JobStatus(val label: String) {
    QUEUED("Queued"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

data class ToolExecutionJob(
    val id: String,
    val toolId: String,
    val actionId: String,
    val target: String,
    val status: JobStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val result: ExecutionResult? = null,
    val errorMessage: String? = null
)
