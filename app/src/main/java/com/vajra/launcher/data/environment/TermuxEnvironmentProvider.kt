package com.vajra.launcher.data.environment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.vajra.launcher.models.CommunicationStatus
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.TermuxLaunchResult

class TermuxEnvironmentProvider(
    private val context: Context,
    val bridge: TermuxBridge = TermuxBridge(context)
) : EnvironmentProvider {

    companion object {
        const val PACKAGE_NAME = "com.termux"
    }

    override val type: EnvironmentType = EnvironmentType.TERMUX

    override fun getInfo(): EnvironmentInfo {
        val pm = context.packageManager
        return try {
            val pInfo = pm.getPackageInfo(PACKAGE_NAME, 0)
            val appName = pInfo.applicationInfo?.loadLabel(pm)?.toString() ?: "Termux"
            val vName = pInfo.versionName ?: "Unknown"
            val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
            val commStatus = bridge.checkCommunicationStatus()

            EnvironmentInfo(
                type = EnvironmentType.TERMUX,
                name = appName,
                isAvailable = true,
                status = if (commStatus == CommunicationStatus.READY) EnvironmentStatus.READY else EnvironmentStatus.AVAILABLE,
                version = vName,
                architecture = arch,
                description = "Termux terminal emulator and Linux userspace package system.",
                packageName = PACKAGE_NAME,
                capabilities = listOf("terminal", "pkg", "run-command")
            )
        } catch (_: PackageManager.NameNotFoundException) {
            EnvironmentInfo(
                type = EnvironmentType.TERMUX,
                name = "Termux",
                isAvailable = false,
                status = EnvironmentStatus.NOT_INSTALLED,
                version = null,
                architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
                description = "Termux is not installed on this Android device.",
                packageName = PACKAGE_NAME
            )
        } catch (e: Exception) {
            EnvironmentInfo(
                type = EnvironmentType.TERMUX,
                name = "Termux",
                isAvailable = false,
                status = EnvironmentStatus.ERROR,
                version = null,
                architecture = null,
                description = "Failed to query Termux package: ${e.localizedMessage ?: "Unknown error"}",
                packageName = PACKAGE_NAME
            )
        }
    }

    fun getCommunicationStatus(): CommunicationStatus {
        return bridge.checkCommunicationStatus()
    }

    override fun isAvailable(): Boolean {
        return try {
            context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launch(callerContext: Context = context): TermuxLaunchResult {
        val pm = callerContext.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: return TermuxLaunchResult.NotInstalled

        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            callerContext.startActivity(launchIntent)
            TermuxLaunchResult.Success
        } catch (e: ActivityNotFoundException) {
            TermuxLaunchResult.NotInstalled
        } catch (e: SecurityException) {
            TermuxLaunchResult.Failed("Security restriction: ${e.localizedMessage ?: "Permission denied"}")
        } catch (e: Exception) {
            TermuxLaunchResult.Failed("Launch error: ${e.localizedMessage ?: "Unexpected error"}")
        }
    }
}
