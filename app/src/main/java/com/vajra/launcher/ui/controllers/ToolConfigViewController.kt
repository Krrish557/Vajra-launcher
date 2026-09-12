package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.vajra.launcher.R
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.ScreenToolConfigBinding

class ToolConfigViewController(
    container: ViewGroup,
    val toolId: String,
    private val onBack: () -> Unit
) {
    val binding: ScreenToolConfigBinding = ScreenToolConfigBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var isServiceDetectChecked = true
    private var isOsDetectChecked = false
    private var isAggressiveChecked = false
    private var isMoreOptionsExpanded = false

    private val scanTypes = arrayOf(
        "SYN Scan (-sS)",
        "Connect Scan (-sT)",
        "UDP Scan (-sU)",
        "FIN Scan (-sF)",
        "Ping Scan (-sn)"
    )

    init {
        val tool = ToolRegistry.getTool(toolId) ?: ToolRegistry.tools.first()
        val context = container.context

        binding.toolConfigTitle.text = "${tool.name} - Advanced"

        binding.toolConfigBackBtn.setOnClickListener {
            onBack()
        }

        // Scan Type Selector Dialog
        binding.spinnerScanType.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select Scan Type")
                .setItems(scanTypes) { _, which ->
                    binding.selectedScanTypeText.text = scanTypes[which]
                }
                .show()
        }

        // Checkbox 1: Service Detection
        binding.checkServiceDetection.setOnClickListener {
            isServiceDetectChecked = !isServiceDetectChecked
            binding.checkServiceDetectionIcon.setImageResource(
                if (isServiceDetectChecked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // Checkbox 2: OS Detection
        binding.checkOsDetection.setOnClickListener {
            isOsDetectChecked = !isOsDetectChecked
            binding.checkOsDetectionIcon.setImageResource(
                if (isOsDetectChecked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // Checkbox 3: Aggressive Scan
        binding.checkAggressiveScan.setOnClickListener {
            isAggressiveChecked = !isAggressiveChecked
            binding.checkAggressiveScanIcon.setImageResource(
                if (isAggressiveChecked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // More options accordion
        binding.btnMoreOptions.setOnClickListener {
            isMoreOptionsExpanded = !isMoreOptionsExpanded
            binding.moreOptionsExpanded.visibility = if (isMoreOptionsExpanded) View.VISIBLE else View.GONE
            binding.moreOptionsChevron.text = if (isMoreOptionsExpanded) "▲" else "▼"
        }

        // Run Scan Action
        binding.btnRunScan.setOnClickListener {
            val target = binding.inputTarget.text.toString().trim()
            val ports = binding.inputPorts.text.toString().trim()
            val scanType = binding.selectedScanTypeText.text.toString()

            if (target.isEmpty()) {
                Toast.makeText(context, "Please specify a scan target", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val flags = mutableListOf<String>()
            if (isServiceDetectChecked) flags.add("-sV")
            if (isOsDetectChecked) flags.add("-O")
            if (isAggressiveChecked) flags.add("-A")
            if (ports.isNotEmpty()) flags.add("-p $ports")

            val constructedCmd = "${tool.executable} $scanType ${flags.joinToString(" ")} $target"

            AlertDialog.Builder(context)
                .setTitle("Scan Prepared")
                .setMessage("Generated Command:\n$constructedCmd\n\nExecution engine will be attached in a subsequent phase.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
