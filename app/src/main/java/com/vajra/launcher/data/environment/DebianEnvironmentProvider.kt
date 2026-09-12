package com.vajra.launcher.data.environment

import android.content.Context
import android.os.Build
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType

class DebianEnvironmentProvider(private val context: Context) : EnvironmentProvider {

    override val type: EnvironmentType = EnvironmentType.DEBIAN

    override fun getInfo(): EnvironmentInfo {
        return EnvironmentInfo(
            type = EnvironmentType.DEBIAN,
            name = "Debian Userspace",
            isAvailable = false,
            status = EnvironmentStatus.NOT_CONFIGURED,
            version = null,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            description = "Debian distribution rootfs managed via PRoot."
        )
    }

    override fun isAvailable(): Boolean = false
}
