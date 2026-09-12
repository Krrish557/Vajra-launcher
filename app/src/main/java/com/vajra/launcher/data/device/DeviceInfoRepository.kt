package com.vajra.launcher.data.device

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.telephony.TelephonyManager
import com.vajra.launcher.environment.RootDetector
import com.vajra.launcher.models.BatteryTelemetry
import com.vajra.launcher.models.ConnectivityTelemetry
import com.vajra.launcher.models.CpuTelemetry
import com.vajra.launcher.models.DeviceDetails
import com.vajra.launcher.models.MemoryTelemetry
import com.vajra.launcher.models.NetworkTelemetry
import com.vajra.launcher.BuildConfig
import com.vajra.launcher.models.StorageTelemetry
import com.vajra.launcher.models.VajraInfo
import java.io.File
import java.io.RandomAccessFile
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale
import kotlin.math.roundToInt

class DeviceInfoRepository(private val context: Context) {

    private var lastCpuTotal: Long = 0
    private var lastCpuIdle: Long = 0

    fun getDeviceDetails(): DeviceDetails {
        val kernelVersion = try {
            val file = File("/proc/version")
            if (file.exists() && file.canRead()) {
                file.readText().split("\\s+".toRegex()).take(3).joinToString(" ")
            } else {
                System.getProperty("os.version") ?: "Linux"
            }
        } catch (_: Exception) {
            System.getProperty("os.version") ?: "Linux"
        }

        return DeviceDetails(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            codename = Build.DEVICE ?: Build.PRODUCT ?: "Unknown",
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            kernel = kernelVersion,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            coreCount = Runtime.getRuntime().availableProcessors(),
            uptime = getUptimeString(),
            rootStatus = RootDetector.detectRootStatus()
        )
    }

    fun getCpuTelemetry(): CpuTelemetry {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"

        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            if (load == null) {
                return CpuTelemetry(0, false, coreCount, arch)
            }

            val toks = load.split("\\s+".toRegex())
            if (toks.size < 5) {
                return CpuTelemetry(0, false, coreCount, arch)
            }

            val idle = toks[4].toLong()
            var total: Long = 0
            for (i in 1 until toks.size) {
                if (i < 8) {
                    total += toks[i].toLongOrNull() ?: 0
                }
            }

            val diffIdle = idle - lastCpuIdle
            val diffTotal = total - lastCpuTotal

            lastCpuIdle = idle
            lastCpuTotal = total

            if (diffTotal > 0) {
                val usage = ((diffTotal - diffIdle).toFloat() / diffTotal.toFloat() * 100).roundToInt()
                CpuTelemetry(usage.coerceIn(0, 100), true, coreCount, arch)
            } else {
                CpuTelemetry(0, true, coreCount, arch)
            }
        } catch (_: Exception) {
            // Do NOT use fabricated fallback. Accurately report isUsageAvailable = false.
            CpuTelemetry(0, false, coreCount, arch)
        }
    }

    fun getMemoryTelemetry(): MemoryTelemetry {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)

            val totalBytes = memInfo.totalMem
            val availBytes = memInfo.availMem
            val usedBytes = (totalBytes - availBytes).coerceAtLeast(0)

            val usedGb = usedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val percent = if (totalBytes > 0) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).roundToInt()
            } else 0

            val formatted = String.format(Locale.US, "%.1f / %.1f GB", usedGb, totalGb)
            MemoryTelemetry(totalBytes, availBytes, usedBytes, formatted, percent.coerceIn(0, 100))
        } catch (_: Exception) {
            MemoryTelemetry(0, 0, 0, "Unavailable", 0)
        }
    }

    fun getStorageTelemetry(): StorageTelemetry {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availBytes).coerceAtLeast(0)

            val usedGb = (usedBytes / (1024L * 1024L * 1024L)).toInt()
            val totalGb = (totalBytes / (1024L * 1024L * 1024L)).toInt()

            val formatted = "$usedGb / $totalGb GB"
            StorageTelemetry(totalBytes, availBytes, usedBytes, formatted, true)
        } catch (_: Exception) {
            StorageTelemetry(0, 0, 0, "Unavailable", false)
        }
    }

    fun getBatteryTelemetry(): BatteryTelemetry {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val healthCode = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val batteryPct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).roundToInt().coerceIn(0, 100)
            } else {
                -1
            }

            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }

            val healthText = when (healthCode) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Normal"
            }

            // Temperature MUST display "Unavailable" if Android does not provide a valid reading.
            // NEVER use a fake fallback such as 39°C!
            val tempText = if (temp > 0) {
                "${temp / 10}°C"
            } else {
                "Unavailable"
            }

            BatteryTelemetry(batteryPct, isCharging, statusText, healthText, tempText)
        } catch (_: Exception) {
            BatteryTelemetry(-1, false, "Unavailable", "Unavailable", "Unavailable")
        }
    }

    fun getNetworkTelemetry(): NetworkTelemetry {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val mobileStatus = when {
            isCellular -> "Connected"
            tm == null -> "Unavailable"
            tm.simState == TelephonyManager.SIM_STATE_ABSENT -> "Not detected"
            tm.simState == TelephonyManager.SIM_STATE_READY -> "Disconnected"
            else -> "Not detected"
        }

        val localIp = getLocalIpAddress()

        val bluetoothStatus = try {
            val bt = BluetoothAdapter.getDefaultAdapter()
            if (bt == null) "Unavailable"
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    if (bt.isEnabled) "On" else "Off"
                } else {
                    "Unavailable"
                }
            } else {
                if (bt.isEnabled) "On" else "Off"
            }
        } catch (_: SecurityException) {
            "Unavailable"
        } catch (_: Exception) {
            "Unavailable"
        }

        return NetworkTelemetry(
            isWifiConnected = isWifi,
            wifiSsid = if (isWifi) "Wi-Fi" else null,
            localIpAddress = localIp,
            mobileNetworkStatus = mobileStatus,
            isVpnActive = isVpn,
            bluetoothStatus = bluetoothStatus
        )
    }

    fun getConnectivityTelemetry(): ConnectivityTelemetry {
        val hasUsbHost = try {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
        } catch (_: Exception) {
            false
        }
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? android.hardware.usb.UsbManager
        val isDeviceConnected = try {
            usbManager?.deviceList?.isNotEmpty() == true
        } catch (_: Exception) {
            false
        }
        return ConnectivityTelemetry(
            isUsbHostSupported = hasUsbHost,
            isOtgAvailable = hasUsbHost,
            isUsbDeviceConnected = isDeviceConnected
        )
    }

    fun getUptimeString(): String {
        val millis = SystemClock.elapsedRealtime()
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }

    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (!host.isNullOrEmpty()) {
                            return host
                        }
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun getVajraInfo(): VajraInfo {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vName = pInfo.versionName ?: BuildConfig.VERSION_NAME
            val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            val codename = if (vName.contains("-")) {
                vName.substringAfter("-").trim()
            } else {
                BuildConfig.POKEMON_CODENAME
            }
            VajraInfo(
                versionName = vName,
                versionCode = vCode,
                packageName = context.packageName,
                codename = codename,
                gitHash = BuildConfig.GIT_HASH
            )
        } catch (_: Exception) {
            VajraInfo(
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE.toLong(),
                packageName = context.packageName,
                codename = BuildConfig.POKEMON_CODENAME,
                gitHash = BuildConfig.GIT_HASH
            )
        }
    }
}
