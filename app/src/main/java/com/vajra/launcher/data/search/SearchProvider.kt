package com.vajra.launcher.data.search

import com.vajra.launcher.R
import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.models.SearchResultItem
import com.vajra.launcher.models.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchProvider(private val appRepository: AppRepository) {

    suspend fun search(query: String, filter: SearchType = SearchType.ALL): List<SearchResultItem> =
        withContext(Dispatchers.Default) {
            val q = query.trim().lowercase()
            val results = mutableListOf<SearchResultItem>()

            // 1. Search Categories
            if (filter == SearchType.ALL || filter == SearchType.CATEGORY) {
                ToolRegistry.categories.forEach { cat ->
                    if (q.isEmpty() || cat.title.lowercase().contains(q) || cat.subtitle.lowercase().contains(q)) {
                        results.add(
                            SearchResultItem(
                                title = cat.title,
                                subtitle = "Category · ${cat.subtitle}",
                                type = SearchType.CATEGORY,
                                iconRes = cat.iconRes,
                                payloadId = cat.id
                            )
                        )
                    }
                }
            }

            // 2. Search Tools
            if (filter == SearchType.ALL || filter == SearchType.TOOL) {
                ToolRegistry.tools.forEach { tool ->
                    if (q.isEmpty() || tool.name.lowercase().contains(q) || tool.description.lowercase().contains(q) || tool.executable.lowercase().contains(q)) {
                        results.add(
                            SearchResultItem(
                                title = tool.name,
                                subtitle = "Tool · ${tool.description}",
                                type = SearchType.TOOL,
                                iconRes = tool.iconRes,
                                payloadId = tool.id
                            )
                        )
                    }
                }
            }

            // 3. Search Commands & Docs
            if (filter == SearchType.ALL || filter == SearchType.COMMAND) {
                ToolRegistry.tools.forEach { tool ->
                    tool.examples.forEach { example ->
                        if (q.isEmpty() || example.lowercase().contains(q)) {
                            results.add(
                                SearchResultItem(
                                    title = example,
                                    subtitle = "Command · ${tool.name}",
                                    type = SearchType.COMMAND,
                                    iconRes = R.drawable.ic_cat_scripts,
                                    payloadId = tool.id
                                )
                            )
                        }
                    }
                }
            }

            if (filter == SearchType.ALL || filter == SearchType.DOC) {
                ToolRegistry.tools.forEach { tool ->
                    val docTitle = "${tool.name} Documentation"
                    if (q.isEmpty() || docTitle.lowercase().contains(q)) {
                        results.add(
                            SearchResultItem(
                                title = docTitle,
                                subtitle = "Document · ${tool.executable} man page",
                                type = SearchType.DOC,
                                iconRes = R.drawable.ic_document,
                                payloadId = tool.id
                            )
                        )
                    }
                }
            }

            // 4. Search Apps
            if (filter == SearchType.ALL || filter == SearchType.APP) {
                val installedApps = appRepository.getInstalledApps()
                installedApps.forEach { app ->
                    if (q.isNotEmpty() && (app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q))) {
                        results.add(
                            SearchResultItem(
                                title = app.label,
                                subtitle = "App · ${app.packageName}",
                                type = SearchType.APP,
                                iconRes = R.drawable.ic_nav_apps,
                                payloadId = app.packageName
                            )
                        )
                    }
                }
            }

            results
        }
}
