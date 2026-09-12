package com.vajra.launcher.ui.controllers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.databinding.ScreenAppsBinding
import com.vajra.launcher.models.AppInfo
import com.vajra.launcher.ui.adapters.AppAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsDrawerViewController(
    container: ViewGroup,
    private val appRepository: AppRepository,
    private val scope: CoroutineScope
) {
    val binding: ScreenAppsBinding = ScreenAppsBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var allApps: List<AppInfo> = emptyList()
    private val appAdapter: AppAdapter

    init {
        val context = container.context
        binding.appsRecyclerView.layoutManager = LinearLayoutManager(context)
        appAdapter = AppAdapter(emptyList()) { app ->
            val launched = appRepository.launchApp(app.packageName)
            if (!launched) {
                Toast.makeText(
                    context,
                    "Cannot launch ${app.label}. App may have been uninstalled or disabled.",
                    Toast.LENGTH_SHORT
                ).show()
                // Force refresh to remove uninstalled app from cached list
                scope.launch {
                    val freshApps = appRepository.getInstalledApps(forceRefresh = true)
                    withContext(Dispatchers.Main) {
                        allApps = freshApps
                        filter(binding.appsSearchInput.text.toString())
                    }
                }
            }
        }
        binding.appsRecyclerView.adapter = appAdapter

        binding.cardAndroidHandoff.setOnClickListener {
            val opened = appRepository.openDefaultLauncher()
            if (!opened) {
                val openedSettings = appRepository.openHomeSettings()
                if (!openedSettings) {
                    Toast.makeText(context, "Cannot open default Android launcher", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.appsSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty()
                binding.appsSearchClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                filter(q)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.appsSearchClear.setOnClickListener {
            binding.appsSearchInput.text.clear()
        }

        scope.launch {
            val apps = appRepository.getInstalledApps()
            withContext(Dispatchers.Main) {
                allApps = apps
                filter(binding.appsSearchInput.text.toString())
            }
        }
    }

    private fun filter(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            allApps
        } else {
            allApps.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }
        appAdapter.updateList(filtered)
    }
}
