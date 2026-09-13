package com.vajra.launcher.data.environment

import android.content.Context
import com.vajra.launcher.models.CommunicationStatus
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.TermuxLaunchResult

class EnvironmentManager(private val context: Context) {

    val termuxBridge = TermuxBridge(context)
    val termuxProvider = TermuxEnvironmentProvider(context, termuxBridge)
    val linuxProvider = LinuxEnvironmentProvider(context, termuxBridge)
    val debianProvider = DebianEnvironmentProvider(context, termuxBridge)

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
                status = EnvironmentStatus.UNKNOWN,
                description = "Unknown environment specification."
            )
    }

    fun isTermuxInstalled(): Boolean {
        return termuxProvider.isAvailable()
    }

    fun getTermuxInfo(): EnvironmentInfo {
        return termuxProvider.getInfo()
    }

    fun getCommunicationStatus(): CommunicationStatus {
        return termuxBridge.checkCommunicationStatus()
    }

    fun getLinuxInfo(): EnvironmentInfo {
        return linuxProvider.getInfo()
    }

    fun getDebianInfo(): EnvironmentInfo {
        return debianProvider.getInfo()
    }

    fun launchTermux(callerContext: Context = context): TermuxLaunchResult {
        return termuxProvider.launch(callerContext)
    }
}
