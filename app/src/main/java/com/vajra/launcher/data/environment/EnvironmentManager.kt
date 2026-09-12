package com.vajra.launcher.data.environment

import android.content.Context
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.TermuxLaunchResult

class EnvironmentManager(private val context: Context) {

    val termuxProvider = TermuxEnvironmentProvider(context)
    val linuxProvider = LinuxEnvironmentProvider(context)
    val debianProvider = DebianEnvironmentProvider(context)

    private val providers = listOf(
        termuxProvider,
        linuxProvider,
        debianProvider
    )

    fun getEnvironments(): List<EnvironmentInfo> {
        return providers.map { it.getInfo() }
    }

    fun getEnvironment(type: EnvironmentType): EnvironmentInfo {
        return providers.firstOrNull { it.type == type }?.getInfo()
            ?: EnvironmentInfo(
                type = type,
                name = type.label,
                isAvailable = false,
                status = com.vajra.launcher.models.EnvironmentStatus.UNKNOWN,
                description = "Unknown environment specification."
            )
    }

    fun isTermuxInstalled(): Boolean {
        return termuxProvider.isAvailable()
    }

    fun getTermuxInfo(): EnvironmentInfo {
        return termuxProvider.getInfo()
    }

    fun launchTermux(callerContext: Context = context): TermuxLaunchResult {
        return termuxProvider.launch(callerContext)
    }
}
