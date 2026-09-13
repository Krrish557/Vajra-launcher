package com.vajra.launcher.data.environment

import android.content.Context
import android.os.Build
import com.vajra.launcher.models.CommunicationStatus
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType

class LinuxEnvironmentProvider(
    private val context: Context,
    private val termuxBridge: TermuxBridge = TermuxBridge(context)
) : EnvironmentProvider {

    override val type: EnvironmentType = EnvironmentType.LINUX

    override fun getInfo(): EnvironmentInfo {
        val commStatus = termuxBridge.checkCommunicationStatus()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"

        return if (commStatus == CommunicationStatus.READY) {
            EnvironmentInfo(
                type = EnvironmentType.LINUX,
                name = "Linux Userspace",
                isAvailable = true,
                status = EnvironmentStatus.READY,
                version = "PRoot Subsystem",
                architecture = arch,
                description = "Linux userspace environment operating via PRoot emulation.",
                capabilities = listOf("proot", "proot-distro")
            )
        } else {
            EnvironmentInfo(
                type = EnvironmentType.LINUX,
                name = "Linux Userspace",
                isAvailable = false,
                status = EnvironmentStatus.NOT_CONFIGURED,
                version = null,
                architecture = arch,
                description = "Generic Linux userspace requires configured Termux communication."
            )
        }
    }

    override fun isAvailable(): Boolean {
        return getInfo().isAvailable
    }
}
