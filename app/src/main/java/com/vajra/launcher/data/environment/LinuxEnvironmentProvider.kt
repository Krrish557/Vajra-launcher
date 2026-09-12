package com.vajra.launcher.data.environment

import android.content.Context
import android.os.Build
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType

class LinuxEnvironmentProvider(private val context: Context) : EnvironmentProvider {

    override val type: EnvironmentType = EnvironmentType.LINUX

    override fun getInfo(): EnvironmentInfo {
        return EnvironmentInfo(
            type = EnvironmentType.LINUX,
            name = "Linux Runtime",
            isAvailable = false,
            status = EnvironmentStatus.NOT_CONFIGURED,
            version = null,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            description = "Generic Linux userspace / PRoot environment runtime."
        )
    }

    override fun isAvailable(): Boolean = false
}
