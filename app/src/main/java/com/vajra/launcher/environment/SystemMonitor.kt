package com.vajra.launcher.environment

import android.content.Context
import com.vajra.launcher.data.device.DeviceInfoRepository
import com.vajra.launcher.models.SystemMetric
import com.vajra.launcher.models.SystemSection

class SystemMonitor(context: Context) {

    val repository = DeviceInfoRepository(context)
    val envManager = com.vajra.launcher.data.environment.EnvironmentManager(context)

    fun getCpuUsagePercent(): Int {
        val cpu = repository.getCpuTelemetry()
        return if (cpu.isUsageAvailable) cpu.usagePercent else 0
    }

    fun getMemoryInfo(): Pair<String, Int> {
        val mem = repository.getMemoryTelemetry()
        return Pair(mem.formattedUsedTotal, mem.usedPercent)
    }

    fun getBatteryInfo(): Triple<Int, Boolean, String> {
        val bat = repository.getBatteryTelemetry()
        val text = if (bat.levelPercent >= 0) {
            if (bat.isCharging) "${bat.levelPercent}% (Charging)" else "${bat.levelPercent}%"
        } else {
            "Unavailable"
        }
        return Triple(bat.levelPercent.coerceAtLeast(0), bat.isCharging, text)
    }

    fun getBatteryTemperature(): String {
        return repository.getBatteryTelemetry().temperatureCelsius
    }

    fun getWifiInfo(): Pair<String, String> {
        val net = repository.getNetworkTelemetry()
        val status = if (net.isWifiConnected) "Connected" else "Disconnected"
        val ip = net.localIpAddress ?: if (net.isWifiConnected) "Connected" else "No Network"
        return Pair(status, ip)
    }

    fun isVpnActive(): Boolean {
        return repository.getNetworkTelemetry().isVpnActive
    }

    fun getUptimeString(): String {
        return repository.getUptimeString()
    }

    fun getStorageInfo(): String {
        return repository.getStorageTelemetry().formattedUsedTotal
    }

    fun getLocalIpAddress(): String? {
        return repository.getLocalIpAddress()
    }

    fun getVajraInfo(): com.vajra.launcher.models.VajraInfo {
        return repository.getVajraInfo()
    }

    fun getSystemInformationSections(): List<SystemSection> {
        val vajra = repository.getVajraInfo()
        val device = repository.getDeviceDetails()
        val cpu = repository.getCpuTelemetry()
        val mem = repository.getMemoryTelemetry()
        val storage = repository.getStorageTelemetry()
        val bat = repository.getBatteryTelemetry()
        val net = repository.getNetworkTelemetry()
        val conn = repository.getConnectivityTelemetry()

        val cpuDisplay = if (cpu.isUsageAvailable) "${cpu.usagePercent}%" else "Unavailable"

        return listOf(
            SystemSection(
                title = "Vajra Launcher",
                metrics = listOf(
                    SystemMetric("Version", vajra.versionName),
                    SystemMetric("Codename", vajra.codename),
                    SystemMetric("Package", vajra.packageName),
                    SystemMetric("Build Code", "${vajra.versionCode}")
                )
            ),
            SystemSection(
                title = "About",
                metrics = listOf(
                    SystemMetric("Repository", vajra.repository),
                    SystemMetric("Summary", vajra.summary)
                )
            ),
            SystemSection(
                title = "Environment",
                metrics = listOf(
                    SystemMetric(
                        "Termux",
                        if (envManager.isTermuxInstalled()) {
                            val v = envManager.getTermuxInfo().version
                            if (v != null) "Available (v$v)" else "Available"
                        } else {
                            "Not installed"
                        }
                    ),
                    SystemMetric("Linux", envManager.linuxProvider.getInfo().status.label),
                    SystemMetric("Debian", envManager.debianProvider.getInfo().status.label)
                )
            ),
            SystemSection(
                title = "Device",
                metrics = listOf(
                    SystemMetric("Manufacturer", device.manufacturer),
                    SystemMetric("Model", device.model),
                    SystemMetric("Codename", device.codename),
                    SystemMetric("Android Version", "${device.androidVersion} (API ${device.sdkVersion})"),
                    SystemMetric("Kernel", device.kernel),
                    SystemMetric("Architecture", device.architecture),
                    SystemMetric("Uptime", device.uptime),
                    SystemMetric("Root", device.rootStatus.label)
                )
            ),
            SystemSection(
                title = "Resources",
                metrics = listOf(
                    SystemMetric("CPU", cpuDisplay),
                    SystemMetric("Cores", "${cpu.coreCount}"),
                    SystemMetric("RAM", mem.formattedUsedTotal),
                    SystemMetric("Storage", storage.formattedUsedTotal),
                    SystemMetric("Battery", if (bat.levelPercent >= 0) "${bat.levelPercent}%" else "Unavailable"),
                    SystemMetric("Temperature", bat.temperatureCelsius)
                )
            ),
            SystemSection(
                title = "Network",
                metrics = listOf(
                    SystemMetric("Wi-Fi", if (net.isWifiConnected) "Connected" else "Disconnected"),
                    SystemMetric("IP Address", net.localIpAddress ?: "Unavailable"),
                    SystemMetric("Mobile Network", net.mobileNetworkStatus),
                    SystemMetric("VPN", if (net.isVpnActive) "Active" else "Off"),
                    SystemMetric("Bluetooth", net.bluetoothStatus)
                )
            ),
            SystemSection(
                title = "Connectivity",
                metrics = listOf(
                    SystemMetric("USB Host", if (conn.isUsbHostSupported) "Supported" else "Unavailable"),
                    SystemMetric("OTG", if (conn.isOtgAvailable) "Supported" else "Unavailable"),
                    SystemMetric("USB Device", if (conn.isUsbDeviceConnected) "Connected" else "Disconnected")
                )
            )
        )
    }
}
