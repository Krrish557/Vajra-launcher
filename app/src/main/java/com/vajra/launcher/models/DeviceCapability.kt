package com.vajra.launcher.models

enum class CapabilityType(val label: String) {
    ROOT("Root Access"),
    TERMUX("Termux Environment"),
    LINUX("Linux Chroot / Proot"),
    DEBIAN("Debian Userspace"),
    ADB("ADB Debugging"),
    USB("USB Subsystem"),
    OTG("USB On-The-Go / Host"),
    WIFI("Wi-Fi Interface"),
    BLUETOOTH("Bluetooth Interface"),
    VPN("Virtual Private Network"),
    CELLULAR("Cellular Subsystem"),
    PACKET_CAPTURE("Raw Packet Capture"),
    ADVANCED_NETWORKING("Monitor Mode / Injection")
}

enum class CapabilityStatus(val label: String) {
    AVAILABLE("Available"),
    UNAVAILABLE("Unavailable"),
    UNKNOWN("Unknown"),
    REQUIRES_ROOT("Requires Root"),
    REQUIRES_HARDWARE("Requires Hardware"),
    REQUIRES_ENVIRONMENT("Requires Environment")
}

data class DeviceCapability(
    val type: CapabilityType,
    val name: String,
    val status: CapabilityStatus,
    val description: String
)
