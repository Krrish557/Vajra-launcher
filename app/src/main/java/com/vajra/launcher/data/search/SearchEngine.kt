package com.vajra.launcher.data.search

import com.vajra.launcher.R
import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.data.tools.ToolRegistry
import com.vajra.launcher.models.SearchResultItem
import com.vajra.launcher.models.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SearchProviderComponent {
    val searchType: SearchType
    suspend fun search(query: String): List<SearchResultItem>
}

class CategorySearchProvider : SearchProviderComponent {
    override val searchType: SearchType = SearchType.CATEGORY

    override suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        ToolRegistry.categories.filter { cat ->
            q.isEmpty() || cat.title.lowercase().contains(q) || cat.subtitle.lowercase().contains(q)
        }.map { cat ->
            SearchResultItem(
                title = cat.title,
                subtitle = "Category · ${cat.subtitle}",
                type = SearchType.CATEGORY,
                iconRes = cat.iconRes,
                payloadId = cat.id
            )
        }
    }
}

class ToolSearchProvider : SearchProviderComponent {
    override val searchType: SearchType = SearchType.TOOL

    override suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        ToolRegistry.tools.filter { tool ->
            q.isEmpty() || tool.name.lowercase().contains(q) || tool.description.lowercase().contains(q) || tool.executable.lowercase().contains(q)
        }.map { tool ->
            SearchResultItem(
                title = tool.name,
                subtitle = "Tool · ${tool.description}",
                type = SearchType.TOOL,
                iconRes = tool.iconRes,
                payloadId = tool.id
            )
        }
    }
}

class CommandSearchProvider : SearchProviderComponent {
    override val searchType: SearchType = SearchType.COMMAND

    override suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        val results = mutableListOf<SearchResultItem>()
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
        results
    }
}

class DocumentationSearchProvider : SearchProviderComponent {
    override val searchType: SearchType = SearchType.DOC

    override suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        ToolRegistry.tools.mapNotNull { tool ->
            val docTitle = "${tool.name} Documentation"
            if (q.isEmpty() || docTitle.lowercase().contains(q)) {
                SearchResultItem(
                    title = docTitle,
                    subtitle = "Document · ${tool.executable} man page",
                    type = SearchType.DOC,
                    iconRes = R.drawable.ic_document,
                    payloadId = tool.id
                )
            } else null
        }
    }
}

class AppSearchProvider(private val appRepository: AppRepository) : SearchProviderComponent {
    override val searchType: SearchType = SearchType.APP

    override suspend fun search(query: String): List<SearchResultItem> = withContext(Dispatchers.Default) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext emptyList()

        val installedApps = appRepository.getInstalledApps()
        installedApps.filter { app ->
            app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q)
        }.map { app ->
            SearchResultItem(
                title = app.label,
                subtitle = "App · ${app.packageName}",
                type = SearchType.APP,
                iconRes = R.drawable.ic_nav_apps,
                payloadId = app.packageName
            )
        }
    }
}

class SearchEngine(
    private val providers: List<SearchProviderComponent>
) {
    suspend fun search(query: String, filter: SearchType = SearchType.ALL): List<SearchResultItem> =
        withContext(Dispatchers.Default) {
            val targetProviders = if (filter == SearchType.ALL) {
                providers
            } else {
                providers.filter { it.searchType == filter }
            }

            val aggregated = mutableListOf<SearchResultItem>()
            for (provider in targetProviders) {
                aggregated.addAll(provider.search(query))
            }
            aggregated
        }
}
