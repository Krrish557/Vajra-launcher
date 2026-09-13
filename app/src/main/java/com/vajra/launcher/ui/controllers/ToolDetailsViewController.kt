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
import com.vajra.launcher.data.tools.ToolStatusResolver
import com.vajra.launcher.databinding.DialogExecutionResultBinding
import com.vajra.launcher.databinding.DialogScanTargetBinding
import com.vajra.launcher.databinding.DialogToolInstallGuideBinding
import com.vajra.launcher.databinding.ScreenToolDetailsBinding
import com.vajra.launcher.models.ExecutionResult
import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolDefinition
import com.vajra.launcher.models.ToolEnvironment
import com.vajra.launcher.models.ToolExecutionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ToolDetailsViewController(
    container: ViewGroup,
    val toolId: String,
    private val toolExecutor: ToolExecutor? = null,
    private val toolStatusResolver: ToolStatusResolver? = null,
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
    private val activeExecutor = toolExecutor ?: ToolExecutor(EnvironmentRouter(envManager))
    private val activeResolver = toolStatusResolver ?: ToolStatusResolver(envManager)
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

        // Initial UI binding using cached status
        val cachedStatus = activeResolver.getCachedStatus(tool)
        updateStatusUI(tool, cachedStatus)

        // Asynchronous live binary verification in background
        activeScope.launch {
            val liveStatus = activeResolver.resolveStatusAsync(tool)
            withContext(Dispatchers.Main) {
                updateStatusUI(tool, liveStatus)
            }
        }
    }

    private fun updateStatusUI(tool: ToolDefinition, status: InstallationState) {
        when (status) {
            InstallationState.INSTALLED -> {
                binding.toolDetailsStatusBadge.text = "● Installed"
                binding.toolDetailsStatusBadge.setBackgroundResource(R.drawable.bg_badge_installed)
                binding.toolDetailsStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
                )

                binding.btnQuickScanText.text = if (tool.id.equals("nmap", ignoreCase = true)) {
                    "QUICK SCAN"
                } else if (tool.id.equals("termux", ignoreCase = true)) {
                    "OPEN TERMUX"
                } else {
                    "EXECUTE"
                }

                binding.btnQuickScan.setOnClickListener {
                    if (tool.id.equals("termux", ignoreCase = true)) {
                        envManager.launchTermux(context)
                    } else {
                        showScanTargetDialog(tool)
                    }
                }
            }
            InstallationState.NOT_INSTALLED -> {
                binding.toolDetailsStatusBadge.text = "● Not Installed"
                binding.toolDetailsStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                binding.toolDetailsStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_badge_error_text)
                )

                binding.btnQuickScanText.text = "INSTALL IN TERMUX"
                binding.btnQuickScan.setOnClickListener {
                    showInstallGuideDialog(tool)
                }
            }
            else -> {
                binding.toolDetailsStatusBadge.text = "● ${status.label}"
                binding.toolDetailsStatusBadge.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                binding.toolDetailsStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.vajra_text_secondary)
                )

                binding.btnQuickScanText.text = "CONFIGURE ENVIRONMENT"
                binding.btnQuickScan.setOnClickListener {
                    showInstallGuideDialog(tool)
                }
            }
        }
    }

    private fun getProposedCommand(tool: ToolDefinition, target: String): String {
        return when (tool.id.lowercase()) {
            "nmap" -> "nmap -sT -Pn -T4 -F $target"
            "netdiscover" -> "netdiscover -r $target"
            "subfinder" -> "subfinder -d $target"
            "whatweb" -> "whatweb $target"
            "nikto" -> "nikto -h $target"
            "sqlmap" -> "sqlmap -u $target --batch"
            else -> "${tool.executable} $target"
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
        val defaultTarget = when (tool.id.lowercase()) {
            "subfinder", "whatweb", "nikto" -> "example.com"
            else -> "127.0.0.1"
        }
        targetBinding.inputScanTarget.setText(defaultTarget)
        targetBinding.txtProposedCommand.text = getProposedCommand(tool, defaultTarget)

        targetBinding.inputScanTarget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString()?.trim().orEmpty()
                targetBinding.txtProposedCommand.text = getProposedCommand(tool, input)
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

    private fun showInstallGuideDialog(tool: ToolDefinition) {
        val guideBinding = DialogToolInstallGuideBinding.inflate(LayoutInflater.from(context))
        guideBinding.guideToolInfo.text = "Tool: ${tool.name} • Environment: ${tool.environment.label}"

        val pkgName = tool.packageName ?: tool.executable
        val installSnippet = tool.installInstructions ?: when (tool.environment) {
            ToolEnvironment.DEBIAN -> "proot-distro login debian\napt update && apt install -y $pkgName"
            ToolEnvironment.TERMUX -> "pkg update && pkg install -y $pkgName"
            else -> "apt update && apt install -y $pkgName"
        }
        guideBinding.txtInstallCommands.text = installSnippet

        val dialog = AlertDialog.Builder(context)
            .setView(guideBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        guideBinding.btnCopyInstallCmd.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Install Commands", installSnippet)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Installation commands copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        guideBinding.btnLaunchTermux.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Install Commands", installSnippet)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Commands copied! Launching Termux...", Toast.LENGTH_SHORT).show()
            envManager.launchTermux(context)
        }

        guideBinding.btnRecheckStatus.setOnClickListener {
            guideBinding.layoutRecheckingProgress.visibility = View.VISIBLE
            guideBinding.txtVerificationFeedback.visibility = View.GONE
            guideBinding.btnRecheckStatus.isEnabled = false

            activeScope.launch {
                val newStatus = activeResolver.resolveStatusAsync(tool, forceRefresh = true)
                withContext(Dispatchers.Main) {
                    guideBinding.layoutRecheckingProgress.visibility = View.GONE
                    guideBinding.btnRecheckStatus.isEnabled = true
                    guideBinding.txtVerificationFeedback.visibility = View.VISIBLE

                    if (newStatus == InstallationState.INSTALLED) {
                        guideBinding.txtVerificationFeedback.text = "✓ Binary detected! Tool is now INSTALLED."
                        guideBinding.txtVerificationFeedback.setTextColor(
                            ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
                        )
                        guideBinding.guideStatusBadge.text = "INSTALLED"
                        guideBinding.guideStatusBadge.setTextColor(
                            ContextCompat.getColor(context, R.color.vajra_badge_installed_text)
                        )
                        guideBinding.guideStatusBadge.setBackgroundResource(R.drawable.bg_badge_installed)
                        updateStatusUI(tool, newStatus)
                    } else {
                        guideBinding.txtVerificationFeedback.text = "✗ Binary '$pkgName' not detected yet in ${tool.environment.label}."
                        guideBinding.txtVerificationFeedback.setTextColor(
                            ContextCompat.getColor(context, R.color.vajra_badge_error_text)
                        )
                    }
                }
            }
        }

        guideBinding.btnDismissGuide.setOnClickListener {
            dialog.dismiss()
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

        resultBinding.txtExecutionOutput.text = if (result.stdout.isNotBlank()) {
            result.stdout
        } else {
            "[No output returned]"
        }

        resultBinding.btnCopyResult.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val outputToCopy = if (result.stdout.isNotBlank()) result.stdout else result.stderr
            val clip = ClipData.newPlainText("Tactical Output", outputToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Scan output copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        resultBinding.btnDismissResult.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
