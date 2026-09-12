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

    private var isOpt1Checked = true
    private var isOpt2Checked = false
    private var isOpt3Checked = false
    private var isMoreOptionsExpanded = false

    init {
        val tool = ToolRegistry.getTool(toolId) ?: ToolRegistry.tools.first()
        val context = container.context

        binding.toolConfigTitle.text = "${tool.name} - Configuration"

        binding.toolConfigBackBtn.setOnClickListener {
            onBack()
        }

        // Generic Scan / Mode options derived from ToolDefinition
        val scanModes = if (tool.quickActions.isNotEmpty()) {
            tool.quickActions.map { "${it.label} (${it.commandTemplate})" }.toTypedArray()
        } else {
            arrayOf(
                "Standard Mode",
                "Fast / Quick Scan",
                "Verbose Analysis",
                "Passive Discovery"
            )
        }

        if (scanModes.isNotEmpty()) {
            binding.selectedScanTypeText.text = scanModes.first()
        }

        binding.spinnerScanType.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Select ${tool.name} Execution Mode")
                .setItems(scanModes) { _, which ->
                    binding.selectedScanTypeText.text = scanModes[which]
                }
                .show()
        }

        // Option 1
        val opt1 = tool.advancedOptions.getOrNull(0)
        isOpt1Checked = opt1?.defaultValue ?: true
        binding.checkServiceDetectionIcon.setImageResource(
            if (isOpt1Checked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.checkServiceDetection.setOnClickListener {
            isOpt1Checked = !isOpt1Checked
            binding.checkServiceDetectionIcon.setImageResource(
                if (isOpt1Checked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // Option 2
        val opt2 = tool.advancedOptions.getOrNull(1)
        isOpt2Checked = opt2?.defaultValue ?: false
        binding.checkOsDetectionIcon.setImageResource(
            if (isOpt2Checked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.checkOsDetection.setOnClickListener {
            isOpt2Checked = !isOpt2Checked
            binding.checkOsDetectionIcon.setImageResource(
                if (isOpt2Checked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // Option 3
        val opt3 = tool.advancedOptions.getOrNull(2)
        isOpt3Checked = opt3?.defaultValue ?: false
        binding.checkAggressiveScanIcon.setImageResource(
            if (isOpt3Checked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
        )
        binding.checkAggressiveScan.setOnClickListener {
            isOpt3Checked = !isOpt3Checked
            binding.checkAggressiveScanIcon.setImageResource(
                if (isOpt3Checked) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            )
        }

        // More options accordion
        binding.btnMoreOptions.setOnClickListener {
            isMoreOptionsExpanded = !isMoreOptionsExpanded
            binding.moreOptionsExpanded.visibility = if (isMoreOptionsExpanded) View.VISIBLE else View.GONE
            binding.moreOptionsChevron.text = if (isMoreOptionsExpanded) "▲" else "▼"
        }

        // Run Scan / Execution trigger
        binding.btnRunScan.setOnClickListener {
            val target = binding.inputTarget.text.toString().trim()
            val ports = binding.inputPorts.text.toString().trim()

            if (target.isEmpty()) {
                Toast.makeText(context, "Please specify a target host or address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val flags = mutableListOf<String>()
            if (isOpt1Checked) opt1?.let { flags.add(it.flag) } ?: flags.add("-v")
            if (isOpt2Checked) opt2?.let { flags.add(it.flag) } ?: flags.add("-sV")
            if (isOpt3Checked) opt3?.let { flags.add(it.flag) } ?: flags.add("-A")
            if (ports.isNotEmpty()) flags.add("-p $ports")

            val constructedCmd = "${tool.executable} ${flags.joinToString(" ")} $target"

            AlertDialog.Builder(context)
                .setTitle("${tool.name} Command Prepared")
                .setMessage("Generated Command:\n$constructedCmd\n\nEnvironment: ${tool.environment.label}\nBinary: ${tool.executable}\n\nExecution engine will be attached in a subsequent phase.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
