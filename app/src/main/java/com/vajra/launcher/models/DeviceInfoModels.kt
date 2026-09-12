package com.vajra.launcher.models

enum class RootStatus(val label: String) {
    AVAILABLE("Available"),
    NOT_AVAILABLE("Not available"),
    UNKNOWN("Unknown")
}

data class VajraInfo(
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val codename: String,
    val gitHash: String = ""
)

data class DeviceDetails(
    val manufacturer: String,
    val model: String,
    val codename: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val kernel: String,
    val architecture: String,
    val coreCount: Int,
    val uptime: String,
    val rootStatus: RootStatus
)

data class CpuTelemetry(
    val usagePercent: Int,
    val isUsageAvailable: Boolean,
    val coreCount: Int,
    val architecture: String
)

data class MemoryTelemetry(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val formattedUsedTotal: String,
    val usedPercent: Int
)

data class StorageTelemetry(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val formattedUsedTotal: String,
    val isAvailable: Boolean
)

data class BatteryTelemetry(
    val levelPercent: Int,
    val isCharging: Boolean,
    val status: String,
    val health: String,
    val temperatureCelsius: String // Real temperature or "Unavailable"
)

data class NetworkTelemetry(
    val isWifiConnected: Boolean,
    val wifiSsid: String?,
    val localIpAddress: String?,
    val mobileNetworkStatus: String, // "Active", "Disconnected", "Not detected", "Unavailable"
    val isVpnActive: Boolean,
    val bluetoothStatus: String // "On", "Off", "Unavailable"
)

data class ConnectivityTelemetry(
    val isUsbHostSupported: Boolean,
    val isOtgAvailable: Boolean,
    val isUsbDeviceConnected: Boolean = false
)
