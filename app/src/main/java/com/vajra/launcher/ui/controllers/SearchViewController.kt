package com.vajra.launcher.ui.controllers

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vajra.launcher.R
import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.data.search.SearchProvider
import com.vajra.launcher.databinding.ScreenSearchBinding
import com.vajra.launcher.models.SearchResultItem
import com.vajra.launcher.models.SearchType
import com.vajra.launcher.ui.adapters.SearchAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewController(
    container: ViewGroup,
    private val searchProvider: SearchProvider,
    private val appRepository: AppRepository,
    private val scope: CoroutineScope,
    initialQuery: String? = null,
    private val onToolSelected: (String) -> Unit,
    private val onCategorySelected: (String) -> Unit,
    private val onBack: () -> Unit
) {
    val binding: ScreenSearchBinding = ScreenSearchBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        false
    )

    private var activeFilter = SearchType.ALL
    private val searchAdapter: SearchAdapter
    private var searchJob: Job? = null

    init {
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(container.context)
        searchAdapter = SearchAdapter(emptyList()) { item ->
            handleItemClick(item, container)
        }
        binding.searchResultsRecyclerView.adapter = searchAdapter

        binding.searchBackBtn.setOnClickListener { onBack() }
        binding.globalSearchClear.setOnClickListener { binding.globalSearchInput.text.clear() }

        setupChips()

        binding.globalSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString().orEmpty()
                binding.globalSearchClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                executeSearch(q)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        if (!initialQuery.isNullOrEmpty()) {
            binding.globalSearchInput.setText(initialQuery)
        } else {
            executeSearch("")
        }
    }

    private fun setupChips() {
        val chips = listOf(
            Pair(binding.chipAll, SearchType.ALL),
            Pair(binding.chipTools, SearchType.TOOL),
            Pair(binding.chipApps, SearchType.APP),
            Pair(binding.chipCommands, SearchType.COMMAND),
            Pair(binding.chipDocs, SearchType.DOC)
        )

        chips.forEach { (chipView, type) ->
            chipView.setOnClickListener {
                activeFilter = type
                updateChipStyles(chips)
                executeSearch(binding.globalSearchInput.text.toString())
            }
        }
    }

    private fun updateChipStyles(chips: List<Pair<TextView, SearchType>>) {
        val context = binding.root.context
        chips.forEach { (chipView, type) ->
            if (type == activeFilter) {
                chipView.setBackgroundResource(R.drawable.bg_badge_installed)
                chipView.setTextColor(ContextCompat.getColor(context, R.color.vajra_text_primary))
            } else {
                chipView.setBackgroundResource(R.drawable.bg_badge_uninstalled)
                chipView.setTextColor(ContextCompat.getColor(context, R.color.vajra_text_secondary))
            }
        }
    }

    private fun executeSearch(query: String) {
        searchJob?.cancel()
        searchJob = scope.launch {
            val results = searchProvider.search(query, activeFilter)
            withContext(Dispatchers.Main) {
                searchAdapter.updateResults(results)
            }
        }
    }

    private fun handleItemClick(item: SearchResultItem, container: ViewGroup) {
        when (item.type) {
            SearchType.TOOL -> onToolSelected(item.payloadId)
            SearchType.CATEGORY -> onCategorySelected(item.payloadId)
            SearchType.APP -> {
                val launched = appRepository.launchApp(item.payloadId)
                if (!launched) {
                    Toast.makeText(container.context, "Failed to launch ${item.title}", Toast.LENGTH_SHORT).show()
                }
            }
            SearchType.COMMAND, SearchType.DOC -> onToolSelected(item.payloadId)
            SearchType.ALL -> {}
        }
    }
}
