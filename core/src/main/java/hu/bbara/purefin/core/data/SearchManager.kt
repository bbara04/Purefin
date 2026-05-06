package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.feature.search.SearchResult
import hu.bbara.purefin.model.Genre
import kotlinx.coroutines.flow.StateFlow

interface SearchManager {
    val searchResult : StateFlow<List<SearchResult>>
    val genres: StateFlow<Set<Genre>>
    fun setGenres(genres : Set<String>)
    fun setSearchTerm(searchTerm: String)
}
