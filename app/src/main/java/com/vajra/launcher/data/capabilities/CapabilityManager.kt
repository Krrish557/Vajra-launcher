package com.vajra.launcher.data.capabilities

import android.content.Context
import android.content.pm.PackageManager
import com.vajra.launcher.environment.RootDetector
import com.vajra.launcher.models.CapabilityStatus
import com.vajra.launcher.models.CapabilityType
import com.vajra.launcher.models.DeviceCapability
import com.vajra.launcher.models.RootStatus

class CapabilityManager(private val context: Context) {

    fun getDeviceCapabilities(): List<DeviceCapability> {
        val pm = context.packageManager
        val rootStatus = RootDetector.detectRootStatus()

        val hasWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI)
        val hasBt = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val hasCellular = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val hasUsbHost = pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        val rootCapabilityStatus = when (rootStatus) {
            RootStatus.AVAILABLE -> CapabilityStatus.AVAILABLE
            RootStatus.NOT_AVAILABLE -> CapabilityStatus.UNAVAILABLE
            RootStatus.UNKNOWN -> CapabilityStatus.UNKNOWN
        }

        return listOf(
            DeviceCapability(
                type = CapabilityType.ROOT,
                name = "Root Access",
                status = rootCapabilityStatus,
                description = when (rootStatus) {
                    RootStatus.AVAILABLE -> "Superuser binary / Magisk detected."
                    RootStatus.NOT_AVAILABLE -> "No superuser binary detected."
                    RootStatus.UNKNOWN -> "Root status could not be verified."
                }
            ),
            DeviceCapability(
                type = CapabilityType.WIFI,
                name = "Wi-Fi Subsystem",
                status = if (hasWifi) CapabilityStatus.AVAILABLE else CapabilityStatus.UNAVAILABLE,
                description = if (hasWifi) "Hardware 802.11 radio present." else "Wi-Fi radio not detected."
            ),
            DeviceCapability(
                type = CapabilityType.BLUETOOTH,
                name = "Bluetooth Subsystem",
                status = if (hasBt) CapabilityStatus.AVAILABLE else CapabilityStatus.UNAVAILABLE,
                description = if (hasBt) "Bluetooth radio controller present." else "Bluetooth hardware not detected."
            ),
            DeviceCapability(
                type = CapabilityType.CELLULAR,
                name = "Cellular Radio",
                status = if (hasCellular) CapabilityStatus.AVAILABLE else CapabilityStatus.UNAVAILABLE,
                description = if (hasCellular) "Telephony baseband hardware present." else "No cellular baseband."
            ),
            DeviceCapability(
                type = CapabilityType.OTG,
                name = "USB Host / OTG",
                status = if (hasUsbHost) CapabilityStatus.AVAILABLE else CapabilityStatus.UNAVAILABLE,
                description = if (hasUsbHost) "USB Host (OTG) peripheral support detected." else "USB Host not reported by platform."
            ),
            DeviceCapability(
                type = CapabilityType.TERMUX,
                name = "Termux Bridge",
                status = CapabilityStatus.UNKNOWN,
                description = "Environment integration deferred to next development block."
            ),
            DeviceCapability(
                type = CapabilityType.LINUX,
                name = "Linux Runtime (Debian / Proot)",
                status = CapabilityStatus.UNKNOWN,
                description = "Linux runtime resolution deferred to next development block."
            ),
            DeviceCapability(
                type = CapabilityType.PACKET_CAPTURE,
                name = "Raw Packet Capture",
                status = if (rootStatus == RootStatus.AVAILABLE) CapabilityStatus.AVAILABLE else CapabilityStatus.REQUIRES_ROOT,
                description = "Raw socket capture capabilities require elevated privileges or root."
            ),
            DeviceCapability(
                type = CapabilityType.ADVANCED_NETWORKING,
                name = "Monitor Mode / Frame Injection",
                status = CapabilityStatus.UNKNOWN,
                description = "Wireless interface driver capabilities dependent on chipset / kernel support."
            )
        )
    }

    fun isRootAvailable(): Boolean = RootDetector.isRootAvailable()

    fun isTermuxInstalled(): CapabilityStatus = CapabilityStatus.UNKNOWN

    fun isLinuxAvailable(): CapabilityStatus = CapabilityStatus.UNKNOWN

    fun isUsbSupported(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

    fun isOtgSupported(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

    fun isWifiAvailable(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)

    fun isBluetoothAvailable(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

    fun isVpnAvailable(): Boolean = true

    fun isCellularAvailable(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
}
