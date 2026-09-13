package com.vajra.launcher.data.environment

import android.content.Context
import android.os.Build
import com.vajra.launcher.models.CommunicationStatus
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType

class DebianEnvironmentProvider(
    private val context: Context,
    private val termuxBridge: TermuxBridge = TermuxBridge(context)
) : EnvironmentProvider {

    override val type: EnvironmentType = EnvironmentType.DEBIAN

    override fun getInfo(): EnvironmentInfo {
        val commStatus = termuxBridge.checkCommunicationStatus()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"

        return if (commStatus == CommunicationStatus.READY) {
            EnvironmentInfo(
                type = EnvironmentType.DEBIAN,
                name = "Debian Userspace",
                isAvailable = true,
                status = EnvironmentStatus.READY,
                version = "Debian GNU/Linux 13",
                architecture = arch,
                description = "Debian distribution container hosted inside Termux PRoot environment.",
                capabilities = listOf("apt", "dpkg", "nmap", "security-tools")
            )
        } else {
            EnvironmentInfo(
                type = EnvironmentType.DEBIAN,
                name = "Debian Userspace",
                isAvailable = false,
                status = EnvironmentStatus.NOT_CONFIGURED,
                version = null,
                architecture = arch,
                description = "Debian distribution requires configured Termux communication."
            )
        }
    }

    override fun isAvailable(): Boolean {
        return getInfo().isAvailable
    }
}
