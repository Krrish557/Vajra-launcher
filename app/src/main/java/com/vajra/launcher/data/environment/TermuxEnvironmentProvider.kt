package com.vajra.launcher.data.environment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.vajra.launcher.models.EnvironmentInfo
import com.vajra.launcher.models.EnvironmentStatus
import com.vajra.launcher.models.EnvironmentType
import com.vajra.launcher.models.TermuxLaunchResult

class TermuxEnvironmentProvider(private val context: Context) : EnvironmentProvider {

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

            EnvironmentInfo(
                type = EnvironmentType.TERMUX,
                name = appName,
                isAvailable = true,
                status = EnvironmentStatus.AVAILABLE,
                version = vName,
                architecture = arch,
                description = "Termux terminal emulator and Linux userspace package system.",
                packageName = PACKAGE_NAME
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

    override fun isAvailable(): Boolean {
        return getInfo().isAvailable
    }

    fun launch(context: Context): TermuxLaunchResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: return TermuxLaunchResult.NotInstalled

        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            TermuxLaunchResult.Success
        } catch (e: ActivityNotFoundException) {
            TermuxLaunchResult.Failed("Termux launch activity was not found: ${e.message}")
        } catch (e: SecurityException) {
            TermuxLaunchResult.Failed("Security restriction prevented launching Termux: ${e.message}")
        } catch (e: Exception) {
            TermuxLaunchResult.Failed("Error opening Termux: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
