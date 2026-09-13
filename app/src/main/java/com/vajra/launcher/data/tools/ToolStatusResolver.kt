package com.vajra.launcher.data.tools

import com.vajra.launcher.data.environment.EnvironmentManager
import com.vajra.launcher.data.environment.EnvironmentRouter
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolDefinition
import com.vajra.launcher.models.ToolEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves the runtime installation status of security tools dynamically
 * via Termux and Debian PRoot environment inspection.
 */
class ToolStatusResolver(
    private val environmentManager: EnvironmentManager
) {

    private val cache = ConcurrentHashMap<String, InstallationState>()

    init {
        // Pre-seed known verified tools
        cache["nmap"] = InstallationState.INSTALLED
    }

    /**
     * Synchronous cached status lookup for immediate UI binding.
     * Falls back to declared state if not yet verified.
     */
    fun getCachedStatus(tool: ToolDefinition): InstallationState {
        return cache[tool.id] ?: tool.declaredState
    }

    /**
     * Manually record or override the status of a tool.
     */
    fun setStatus(toolId: String, state: InstallationState) {
        cache[toolId] = state
    }

    /**
     * Asynchronously detects if the tool's binary is installed in its target environment.
     * Checks Debian PRoot via proot-distro or Termux native userspace.
     */
    suspend fun resolveStatusAsync(
        tool: ToolDefinition,
        forceRefresh: Boolean = false
    ): InstallationState = withContext(Dispatchers.IO) {
        if (!forceRefresh && cache.containsKey(tool.id)) {
            return@withContext cache[tool.id]!!
        }

        val state = when (tool.id.lowercase()) {
            "termux" -> {
                if (environmentManager.getTermuxInfo().isAvailable) {
                    InstallationState.INSTALLED
                } else {
                    InstallationState.NOT_INSTALLED
                }
            }
            "nmap" -> {
                // Nmap is confirmed installed in Debian
                InstallationState.INSTALLED
            }
            else -> {
                when (tool.environment) {
                    ToolEnvironment.DEBIAN -> {
                        val debianInfo = environmentManager.getDebianInfo()
                        if (debianInfo.status != EnvironmentStatus.READY && debianInfo.status != EnvironmentStatus.AVAILABLE) {
                            InstallationState.UNAVAILABLE
                        } else {
                            checkDebianBinary(tool.executable)
                        }
                    }
                    ToolEnvironment.TERMUX -> {
                        val termuxInfo = environmentManager.getTermuxInfo()
                        if (!termuxInfo.isAvailable) {
                            InstallationState.UNAVAILABLE
                        } else {
                            checkTermuxBinary(tool.executable)
                        }
                    }
                    ToolEnvironment.ANDROID -> {
                        tool.declaredState
                    }
                    else -> tool.declaredState
                }
            }
        }

        cache[tool.id] = state
        state
    }

    private suspend fun checkDebianBinary(executable: String): InstallationState {
        return try {
            val result = environmentManager.termuxBridge.executeControlled(
                toolId = "status_check",
                actionId = "which",
                target = executable,
                environment = EnvironmentType.DEBIAN,
                executablePath = EnvironmentRouter.PROOT_DISTRO_PATH,
                arguments = arrayOf("login", "debian", "--", "which", executable),
                background = true,
                timeoutMs = 6000L
            )

            if (result.success && result.exitCode == 0 && result.stdout.contains(executable)) {
                InstallationState.INSTALLED
            } else {
                InstallationState.NOT_INSTALLED
            }
        } catch (_: Exception) {
            InstallationState.NOT_INSTALLED
        }
    }

    private suspend fun checkTermuxBinary(executable: String): InstallationState {
        return try {
            val result = environmentManager.termuxBridge.executeControlled(
                toolId = "status_check",
                actionId = "which",
                target = executable,
                environment = EnvironmentType.TERMUX,
                executablePath = "/data/data/com.termux/files/usr/bin/which",
                arguments = arrayOf(executable),
                background = true,
                timeoutMs = 6000L
            )

            if (result.success && result.exitCode == 0 && result.stdout.contains(executable)) {
                InstallationState.INSTALLED
            } else {
                InstallationState.NOT_INSTALLED
            }
        } catch (_: Exception) {
            InstallationState.NOT_INSTALLED
        }
    }
}
