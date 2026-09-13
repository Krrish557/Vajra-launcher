package com.vajra.launcher.data.environment

import com.vajra.launcher.data.executor.TargetValidator
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.ExecutionResult
import com.vajra.launcher.models.ToolEnvironment
import com.vajra.launcher.models.ToolExecutionRequest
import java.util.UUID

class EnvironmentRouter(
    private val environmentManager: EnvironmentManager
) {

    companion object {
        const val PROOT_DISTRO_PATH = "/data/data/com.termux/files/usr/bin/proot-distro"
    }

    suspend fun routeAndExecute(request: ToolExecutionRequest): ExecutionResult {
        val sanitizedTarget = TargetValidator.sanitize(request.target)
        if (!TargetValidator.isValid(sanitizedTarget)) {
            return ExecutionResult(
                id = UUID.randomUUID().toString(),
                toolId = request.toolId,
                actionId = request.actionId,
                target = request.target,
                environment = EnvironmentType.UNKNOWN,
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Invalid target '${request.target}'. Target must be a valid IPv4, IPv6, or hostname with no special characters.",
                durationMs = 0L
            )
        }

        val tool = ToolRegistry.getTool(request.toolId)
            ?: return ExecutionResult(
                id = UUID.randomUUID().toString(),
                toolId = request.toolId,
                actionId = request.actionId,
                target = sanitizedTarget,
                environment = EnvironmentType.UNKNOWN,
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Unknown tool '${request.toolId}'.",
                durationMs = 0L
            )

        val requiredEnv = when (tool.environment) {
            ToolEnvironment.DEBIAN -> EnvironmentType.DEBIAN
            ToolEnvironment.TERMUX -> EnvironmentType.TERMUX
            ToolEnvironment.ANDROID -> EnvironmentType.ANDROID
            ToolEnvironment.EXTERNAL_APP -> EnvironmentType.EXTERNAL_APP
            ToolEnvironment.LINUX -> EnvironmentType.LINUX
            ToolEnvironment.UNKNOWN -> EnvironmentType.UNKNOWN
        }

        return when (requiredEnv) {
            EnvironmentType.DEBIAN -> {
                val debianInfo = environmentManager.getDebianInfo()
                if (debianInfo.status != EnvironmentStatus.READY && debianInfo.status != EnvironmentStatus.AVAILABLE) {
                    return ExecutionResult(
                        id = UUID.randomUUID().toString(),
                        toolId = request.toolId,
                        actionId = request.actionId,
                        target = sanitizedTarget,
                        environment = EnvironmentType.DEBIAN,
                        success = false,
                        exitCode = -1,
                        stdout = "",
                        stderr = "Debian environment is not ready (Status: ${debianInfo.status.label}). Ensure Termux is running and proot-distro Debian is configured.",
                        durationMs = 0L
                    )
                }

                // Assemble controlled arguments array based on tool & action
                val toolArgs = when (tool.id.lowercase()) {
                    "nmap" -> {
                        when (request.actionId) {
                            "service_scan" -> arrayOf("login", "debian", "--", "nmap", "-sT", "-Pn", "-sV", "-T4", sanitizedTarget)
                            else -> arrayOf("login", "debian", "--", "nmap", "-sT", "-Pn", "-T4", "-F", sanitizedTarget)
                        }
                    }
                    "netdiscover" -> arrayOf("login", "debian", "--", "netdiscover", "-r", sanitizedTarget)
                    "whatweb" -> arrayOf("login", "debian", "--", "whatweb", sanitizedTarget)
                    "subfinder" -> arrayOf("login", "debian", "--", "subfinder", "-d", sanitizedTarget)
                    "nikto" -> arrayOf("login", "debian", "--", "nikto", "-h", sanitizedTarget)
                    "sqlmap" -> arrayOf("login", "debian", "--", "sqlmap", "-u", sanitizedTarget, "--batch")
                    else -> arrayOf("login", "debian", "--", tool.executable, sanitizedTarget)
                }

                environmentManager.termuxBridge.executeControlled(
                    toolId = request.toolId,
                    actionId = request.actionId,
                    target = sanitizedTarget,
                    environment = EnvironmentType.DEBIAN,
                    executablePath = PROOT_DISTRO_PATH,
                    arguments = toolArgs,
                    background = true
                )
            }
            EnvironmentType.TERMUX -> {
                val termuxInfo = environmentManager.getTermuxInfo()
                if (!termuxInfo.isAvailable) {
                    return ExecutionResult(
                        id = UUID.randomUUID().toString(),
                        toolId = request.toolId,
                        actionId = request.actionId,
                        target = sanitizedTarget,
                        environment = EnvironmentType.TERMUX,
                        success = false,
                        exitCode = -1,
                        stdout = "",
                        stderr = "Termux is not installed.",
                        durationMs = 0L
                    )
                }

                val execPath = "/data/data/com.termux/files/usr/bin/${tool.executable}"
                val toolArgs = arrayOf(sanitizedTarget)

                environmentManager.termuxBridge.executeControlled(
                    toolId = request.toolId,
                    actionId = request.actionId,
                    target = sanitizedTarget,
                    environment = EnvironmentType.TERMUX,
                    executablePath = execPath,
                    arguments = toolArgs,
                    background = true
                )
            }
            else -> {
                ExecutionResult(
                    id = UUID.randomUUID().toString(),
                    toolId = request.toolId,
                    actionId = request.actionId,
                    target = sanitizedTarget,
                    environment = requiredEnv,
                    success = false,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Environment ${requiredEnv.label} is not executable via Termux bridge.",
                    durationMs = 0L
                )
            }
        }
    }
}
