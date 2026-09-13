package com.vajra.launcher.ui.controllers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.vajra.launcher.R
import com.vajra.launcher.data.environment.EnvironmentManager
import com.vajra.launcher.data.environment.EnvironmentRouter
import com.vajra.launcher.data.executor.TargetValidator
import com.vajra.launcher.data.executor.ToolExecutor
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.DialogExecutionResultBinding
import com.vajra.launcher.databinding.DialogScanTargetBinding
import com.vajra.launcher.databinding.ScreenToolDetailsBinding
import com.vajra.launcher.models.ExecutionResult
import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolDefinition
import com.vajra.launcher.models.ToolExecutionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ToolDetailsViewController(
    container: ViewGroup,
    val toolId: String,
    private val toolExecutor: ToolExecutor? = null,
    private val scope: CoroutineScope? = null,
    private val onAdvConfigSelected: (String) -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenToolDetailsBinding = ScreenToolDetailsBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private val context = container.context
    private val envManager = EnvironmentManager(context)
    private val activeExecutor = toolExecutor ?: ToolExecutor(
        EnvironmentRouter(envManager)
    )
    private val activeScope = scope ?: CoroutineScope(Dispatchers.Main)

    init {
        val tool = ToolRegistry.getTool(toolId) ?: ToolRegistry.tools.first()

        binding.toolDetailsTitle.text = tool.name
        binding.toolDetailsCategory.text = tool.description
        binding.toolDetailsDescription.text = tool.fullDescription
        binding.toolDetailsIcon.setImageResource(tool.iconRes)
        binding.toolDetailsEnvironment.text = tool.environment.label
        binding.toolDetailsVersion.text = tool.version ?: "N/A"
        binding.toolDetailsExecutable.text = tool.executable

        // Status badge
        when (tool.state) {
            InstallationState.INSTALLED -> {
                binding.toolDetailsStatusBadge.text = "● ${tool.state.label}"
                binding.toolDetailsStatusBadge.setBackgroundResource(R.drawable.bg_badge_installed)
                binding.toolDetailsStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
                )
            }
            else -> {
                binding.toolDetailsStatusBadge.text = tool.state.label
                binding.toolDetailsStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                binding.toolDetailsStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_uninstalled_text)
                )
            }
        }

        // Examples
        if (tool.examples.isNotEmpty()) {
            binding.toolExampleText.text = tool.examples.joinToString("\n")
        } else {
            binding.toolExampleText.text = "${tool.executable} -h"
        }

        binding.toolDetailsBackBtn.setOnClickListener {
            onBack()
        }

        binding.toolDetailsMenuBtn.setOnClickListener {
            Toast.makeText(context, "${tool.name} options", Toast.LENGTH_SHORT).show()
        }

        // Configure Primary Action Button
        if (tool.id.equals("termux", ignoreCase = true)) {
            binding.btnQuickScanText.text = "OPEN TERMUX"
            binding.btnQuickScan.setOnClickListener {
                when (val res = envManager.launchTermux(context)) {
                    is com.vajra.launcher.models.TermuxLaunchResult.Success -> {
                        // Launched successfully
                    }
                    is com.vajra.launcher.models.TermuxLaunchResult.NotInstalled -> {
                        Toast.makeText(context, "Termux is not installed on this device.", Toast.LENGTH_SHORT).show()
                    }
                    is com.vajra.launcher.models.TermuxLaunchResult.Failed -> {
                        Toast.makeText(context, res.reason, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else if (tool.id.equals("nmap", ignoreCase = true)) {
            binding.btnQuickScanText.text = "QUICK SCAN"
            binding.btnQuickScan.setOnClickListener {
                showScanTargetDialog(tool)
            }
        } else {
            val firstAction = tool.quickActions.firstOrNull()
            val actionLabel = firstAction?.label?.uppercase() ?: "EXECUTE"
            binding.btnQuickScanText.text = actionLabel
            binding.btnQuickScan.setOnClickListener {
                val cmd = firstAction?.commandTemplate ?: "${tool.executable} {target}"
                Toast.makeText(
                    context,
                    "$actionLabel: $cmd (Requires configured tool environment)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.btnAdvConfig.setOnClickListener {
            onAdvConfigSelected(tool.id)
        }

        binding.btnViewDocs.setOnClickListener {
            val docs = tool.documentation
            val syntaxMsg = docs?.syntax?.let { "\n\nSyntax:\n$it" } ?: ""
            val manMsg = docs?.manPage?.let { "\nMan Page: $it" } ?: ""
            val urlMsg = docs?.docsUrl?.let { "\nDocumentation: $it" } ?: ""

            AlertDialog.Builder(context)
                .setTitle("${tool.name} Manual")
                .setMessage("Environment: ${tool.environment.label}\nBinary: ${tool.executable}\nVersion: ${tool.version ?: "N/A"}$manMsg$urlMsg\n\n${tool.fullDescription}$syntaxMsg\n\nUsage Examples:\n" + tool.examples.joinToString("\n"))
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun showScanTargetDialog(tool: ToolDefinition) {
        val targetBinding = DialogScanTargetBinding.inflate(LayoutInflater.from(context))
        targetBinding.dialogTargetSubtitle.text = "Target environment: ${tool.environment.label} • Binary: ${tool.executable}"

        val dialog = AlertDialog.Builder(context)
            .setView(targetBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Pre-populate target
        targetBinding.inputScanTarget.setText("127.0.0.1")
        targetBinding.txtProposedCommand.text = "nmap -sT -Pn -T4 -F 127.0.0.1"

        targetBinding.inputScanTarget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString()?.trim().orEmpty()
                targetBinding.txtProposedCommand.text = "nmap -sT -Pn -T4 -F $input"
                targetBinding.txtTargetError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        targetBinding.btnCancelScan.setOnClickListener {
            dialog.dismiss()
        }

        targetBinding.btnConfirmScan.setOnClickListener {
            val rawTarget = targetBinding.inputScanTarget.text.toString().trim()
            val isValid = TargetValidator.isValid(rawTarget)

            if (!isValid) {
                targetBinding.txtTargetError.text = "Invalid target: Valid IPv4, IPv6, or safe hostname required."
                targetBinding.txtTargetError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Target is valid - initiate execution
            targetBinding.txtTargetError.visibility = View.GONE
            targetBinding.layoutScanButtons.visibility = View.GONE
            targetBinding.layoutScanProgress.visibility = View.VISIBLE
            targetBinding.inputScanTarget.isEnabled = false
            dialog.setCancelable(false)

            activeScope.launch {
                val request = ToolExecutionRequest(
                    toolId = tool.id,
                    actionId = "quick_scan",
                    target = rawTarget,
                    options = emptyMap()
                )

                val result = activeExecutor.execute(request)
                dialog.dismiss()
                showExecutionResultDialog(tool, rawTarget, result)
            }
        }

        dialog.show()
    }

    private fun showExecutionResultDialog(
        tool: ToolDefinition,
        target: String,
        result: ExecutionResult
    ) {
        val resultBinding = DialogExecutionResultBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(resultBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resultBinding.resultToolInfo.text = "Tool: ${tool.name} v${tool.version ?: "7.95"} • Environment: ${tool.environment.label}"
        resultBinding.resultTarget.text = target
        resultBinding.resultExitCode.text = "${result.exitCode}"
        val durationFormatted = String.format(Locale.US, "%.2fs", result.durationMs / 1000.0)
        resultBinding.resultDuration.text = durationFormatted

        if (result.success) {
            resultBinding.resultStatusBadge.text = "SUCCESS"
            resultBinding.resultStatusBadge.setBackgroundResource(R.drawable.bg_badge_installed)
            resultBinding.resultStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
            )
            resultBinding.txtExecutionError.visibility = View.GONE
        } else {
            resultBinding.resultStatusBadge.text = "FAILED"
            resultBinding.resultStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
            resultBinding.resultStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.vajra_badge_error_text)
            )
            if (result.stderr.isNotBlank()) {
                resultBinding.txtExecutionError.text = "STDERR:\n${result.stderr}"
                resultBinding.txtExecutionError.visibility = View.VISIBLE
            }
        }

        val outputContent = if (result.stdout.isNotBlank()) {
            result.stdout
        } else if (result.stderr.isNotBlank()) {
            "[No stdout - See Stderr below]\n${result.stderr}"
        } else {
            "[Process finished with exit code ${result.exitCode} - No output]"
        }

        resultBinding.txtExecutionOutput.text = outputContent

        resultBinding.btnCopyResult.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Vajra Scan Result", outputContent)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Scan output copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        resultBinding.btnDismissResult.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
