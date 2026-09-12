package com.vajra.launcher.environment

import android.os.Build
import com.vajra.launcher.models.RootStatus
import java.io.File

object RootDetector {

    private val SU_PATHS = arrayOf(
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/app/Superuser.apk"
    )

    private val MAGISK_PATHS = arrayOf(
        "/data/adb/magisk",
        "/data/adb/modules",
        "/sbin/.magisk",
        "/cache/.disable_magisk"
    )

    fun detectRootStatus(): RootStatus {
        return try {
            if (checkSuBinary() || checkMagiskDirectories() || checkPathForSu() || checkBuildTags()) {
                RootStatus.AVAILABLE
            } else {
                RootStatus.NOT_AVAILABLE
            }
        } catch (_: SecurityException) {
            RootStatus.UNKNOWN
        } catch (_: Exception) {
            RootStatus.UNKNOWN
        }
    }

    fun isRootAvailable(): Boolean {
        return detectRootStatus() == RootStatus.AVAILABLE
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuBinary(): Boolean {
        for (path in SU_PATHS) {
            try {
                val file = File(path)
                if (file.exists() && file.canExecute()) {
                    return true
                }
                if (file.exists()) {
                    return true
                }
            } catch (_: Exception) {
            }
        }
        return false
    }

    private fun checkMagiskDirectories(): Boolean {
        for (path in MAGISK_PATHS) {
            try {
                val file = File(path)
                if (file.exists()) {
                    return true
                }
            } catch (_: Exception) {
            }
        }
        return false
    }

    private fun checkPathForSu(): Boolean {
        val pathEnv = System.getenv("PATH") ?: return false
        val dirs = pathEnv.split(File.pathSeparatorChar)
        for (dir in dirs) {
            try {
                val suFile = File(dir, "su")
                if (suFile.exists()) {
                    return true
                }
            } catch (_: Exception) {
            }
        }
        return false
    }
}
