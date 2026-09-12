package com.vajra.launcher.ui.controllers

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vajra.launcher.databinding.ScreenHomeBinding
import com.vajra.launcher.environment.SystemMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewController(
    container: ViewGroup,
    private val systemMonitor: SystemMonitor,
    private val onNavigateToSystem: () -> Unit,
    private val onNavigateToSearch: () -> Unit
) {
    val binding: ScreenHomeBinding = ScreenHomeBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var pollJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    init {
        binding.homeSearchBtn.setOnClickListener { onNavigateToSearch() }
        binding.cardCpu.setOnClickListener { onNavigateToSystem() }
        binding.cardRam.setOnClickListener { onNavigateToSystem() }
        binding.cardBattery.setOnClickListener { onNavigateToSystem() }
        binding.cardTemp.setOnClickListener { onNavigateToSystem() }
        binding.cardWifi.setOnClickListener { onNavigateToSystem() }
        binding.cardVpn.setOnClickListener { onNavigateToSystem() }
        binding.cardUptime.setOnClickListener { onNavigateToSystem() }
    }

    fun start(scope: CoroutineScope) {
        updateTimeAndDate()

        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                updateMetrics()
                delay(3000)
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun updateTimeAndDate() {
        val now = Date()
        binding.homeClockText.text = timeFormat.format(now)
        binding.homeDateText.text = dateFormat.format(now)
    }

    private suspend fun updateMetrics() {
        val now = Date()
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)

        val cpu = withContext(Dispatchers.IO) { systemMonitor.getCpuUsagePercent() }
        val mem = withContext(Dispatchers.IO) { systemMonitor.getMemoryInfo() }
        val battery = withContext(Dispatchers.IO) { systemMonitor.getBatteryInfo() }
        val temp = withContext(Dispatchers.IO) { systemMonitor.getBatteryTemperature() }
        val wifi = withContext(Dispatchers.IO) { systemMonitor.getWifiInfo() }
        val isVpn = withContext(Dispatchers.IO) { systemMonitor.isVpnActive() }
        val uptime = withContext(Dispatchers.IO) { systemMonitor.getUptimeString() }

        withContext(Dispatchers.Main) {
            binding.homeClockText.text = timeStr
            binding.homeDateText.text = dateStr

            binding.cpuValueText.text = "$cpu%"
            binding.cpuProgressBar.progress = cpu

            binding.ramValueText.text = mem.first
            binding.ramProgressBar.progress = mem.second

            binding.batteryValueText.text = "${battery.first}%"
            binding.batteryProgressBar.progress = battery.first

            binding.tempValueText.text = temp

            binding.wifiStatusText.text = wifi.first
            binding.wifiIpText.text = wifi.second

            binding.vpnStatusText.text = if (isVpn) "Active" else "Off"
            binding.uptimeStatusText.text = uptime
        }
    }
}
