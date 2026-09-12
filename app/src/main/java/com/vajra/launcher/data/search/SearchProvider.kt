package com.vajra.launcher.data.search

import com.vajra.launcher.data.apps.AppRepository
import com.vajra.launcher.models.SearchResultItem
import com.vajra.launcher.models.SearchType

class SearchProvider(appRepository: AppRepository) {

    private val searchEngine = SearchEngine(
        listOf(
            CategorySearchProvider(),
            ToolSearchProvider(),
            CommandSearchProvider(),
            DocumentationSearchProvider(),
            AppSearchProvider(appRepository)
        )
    )

    suspend fun search(query: String, filter: SearchType = SearchType.ALL): List<SearchResultItem> {
        return searchEngine.search(query, filter)
    }
}
