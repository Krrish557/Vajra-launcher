package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.vajra.launcher.R
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.databinding.ScreenToolDetailsBinding
import com.vajra.launcher.models.InstallationState

class ToolDetailsViewController(
    container: ViewGroup,
    val toolId: String,
    private val onAdvConfigSelected: (String) -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenToolDetailsBinding = ScreenToolDetailsBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    init {
        val tool = ToolRegistry.getTool(toolId) ?: ToolRegistry.tools.first()
        val context = container.context

        binding.toolDetailsTitle.text = tool.name
        binding.toolDetailsCategory.text = tool.description
        binding.toolDetailsDescription.text = tool.fullDescription
        binding.toolDetailsIcon.setImageResource(tool.iconRes)

        // Status badge
        when (tool.state) {
            InstallationState.INSTALLED -> {
                binding.toolDetailsStatusBadge.text = tool.state.label
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

        val envManager = com.vajra.launcher.data.environment.EnvironmentManager(context)

        if (tool.id.equals("termux", ignoreCase = true)) {
            binding.btnQuickScanText.text = "OPEN TERMUX"
        } else {
            val firstAction = tool.quickActions.firstOrNull()
            if (firstAction != null) {
                binding.btnQuickScanText.text = firstAction.label.uppercase()
            }
        }

        binding.btnQuickScan.setOnClickListener {
            if (tool.id.equals("termux", ignoreCase = true)) {
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
            } else {
                val action = tool.quickActions.firstOrNull()
                val actionName = action?.label ?: "Quick Action"
                val cmd = action?.commandTemplate ?: "${tool.executable} {target}"
                Toast.makeText(
                    context,
                    "$actionName: $cmd (Execution engine deferred in v0.1)",
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
}
