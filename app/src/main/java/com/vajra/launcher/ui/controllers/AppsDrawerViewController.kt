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
    scope: CoroutineScope
) {
    val binding: ScreenAppsBinding = ScreenAppsBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var allApps: List<AppInfo> = emptyList()
    private val appAdapter: AppAdapter

    init {
        binding.appsRecyclerView.layoutManager = LinearLayoutManager(container.context)
        appAdapter = AppAdapter(emptyList()) { app ->
            val launched = appRepository.launchApp(app.packageName)
            if (!launched) {
                Toast.makeText(container.context, "Failed to launch ${app.label}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.appsRecyclerView.adapter = appAdapter

        binding.cardAndroidHandoff.setOnClickListener {
            val opened = appRepository.openDefaultLauncher()
            if (!opened) {
                Toast.makeText(container.context, "Cannot open default launcher", Toast.LENGTH_SHORT).show()
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
