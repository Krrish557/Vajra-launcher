package com.vajra.launcher.data.environment

import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentType

/**
 * Common contract for environment resolvers in Vajra.
 */
interface EnvironmentProvider {
    val type: EnvironmentType
    fun getInfo(): EnvironmentInfo
    fun isAvailable(): Boolean
}
