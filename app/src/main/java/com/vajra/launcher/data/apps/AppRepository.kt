package com.vajra.launcher.data.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.vajra.launcher.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private var cachedApps: List<AppInfo>? = null

    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedApps != null) {
            return@withContext cachedApps!!
        }

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            // Exclude Vajra itself to prevent self-referential app listing
            if (packageName == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            AppInfo(
                label = label,
                packageName = packageName,
                icon = icon
            )
        }.sortedBy { it.label.lowercase() }

        cachedApps = appList
        appList
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: ActivityNotFoundException) {
            // App was uninstalled or disabled
            cachedApps = null
            false
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Safely hands off execution to the default/alternate Android Home launcher.
     * Prevents user from being trapped if Vajra is selected as default.
     */
    fun openDefaultLauncher(): Boolean {
        return try {
            val pm = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val allHomeActivities = pm.queryIntentActivities(homeIntent, 0)
            // Filter out Vajra itself and Android's FallbackHome stub
            val alternateLaunchers = allHomeActivities.filter {
                it.activityInfo.packageName != context.packageName &&
                        it.activityInfo.packageName != "com.android.settings" &&
                        !it.activityInfo.name.contains("FallbackHome")
            }

            when {
                alternateLaunchers.size == 1 -> {
                    // Exactly 1 alternate launcher (e.g. Samsung One UI, LineageOS Trebuchet)
                    val target = alternateLaunchers.first().activityInfo
                    val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        setClassName(target.packageName, target.name)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(explicitIntent)
                    true
                }
                alternateLaunchers.size > 1 -> {
                    // Multiple alternate launchers: present chooser
                    val chooserIntent = Intent.createChooser(homeIntent, "Select Home Launcher").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(chooserIntent)
                    true
                }
                else -> {
                    // No alternate launcher detected: open Home Settings so user can safely reconfigure
                    openHomeSettings()
                }
            }
        } catch (_: Exception) {
            openHomeSettings()
        }
    }

    fun openHomeSettings(): Boolean {
        return try {
            val settingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            true
        } catch (_: Exception) {
            openSystemSettings()
        }
    }

    fun openSystemSettings(): Boolean {
        return try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether Vajra is currently configured as the system default home launcher.
     */
    fun isVajraDefaultLauncher(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val defaultPkg = resolveInfo?.activityInfo?.packageName
            defaultPkg == context.packageName
        } catch (_: Exception) {
            false
        }
    }
}
