package com.vajra.launcher.ui.controllers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vajra.launcher.R
import com.vajra.launcher.databinding.ItemSystemMetricRowBinding
import com.vajra.launcher.databinding.ScreenSystemBinding
import com.vajra.launcher.environment.SystemMonitor
import com.vajra.launcher.models.SystemSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SystemInfoViewController(
    container: ViewGroup,
    private val systemMonitor: SystemMonitor,
    private val onOpenCustomization: () -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenSystemBinding = ScreenSystemBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var pollJob: Job? = null

    init {
        binding.systemBackBtn.setOnClickListener { onBack() }
        binding.systemSettingsBtn.setOnClickListener { onOpenCustomization() }
        binding.cardSystemSettingsShortcut.setOnClickListener {
            val context = container.context
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                android.widget.Toast.makeText(context, "Cannot open System Settings", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun start(scope: CoroutineScope) {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                val sections = withContext(Dispatchers.IO) {
                    systemMonitor.getSystemInformationSections()
                }
                withContext(Dispatchers.Main) {
                    populateSections(sections)
                }
                delay(3000)
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun populateSections(sections: List<SystemSection>) {
        val container = binding.systemSectionsContainer
        container.removeAllViews()
        val context = container.context
        val inflater = LayoutInflater.from(context)

        sections.forEach { section ->
            // Section Card
            val sectionCard = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 14.dpToPx(context))
                }
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card_cyber)
                setPadding(14.dpToPx(context), 12.dpToPx(context), 14.dpToPx(context), 12.dpToPx(context))
            }

            // Section Title Header
            val titleView = TextView(context).apply {
                text = section.title.uppercase()
                setTextColor(ContextCompat.getColor(context, R.color.vajra_accent_blue))
                textSize = 12f
                letterSpacing = 0.08f
                setPadding(0, 0, 0, 8.dpToPx(context))
            }
            sectionCard.addView(titleView)

            // Metric Rows
            section.metrics.forEach { metric ->
                val rowBinding = ItemSystemMetricRowBinding.inflate(inflater, sectionCard, false)
                rowBinding.metricLabel.text = metric.label
                rowBinding.metricValue.text = metric.value
                sectionCard.addView(rowBinding.root)
            }

            container.addView(sectionCard)
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
