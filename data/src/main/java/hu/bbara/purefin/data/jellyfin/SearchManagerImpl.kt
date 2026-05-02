package hu.bbara.purefin.data.jellyfin

import hu.bbara.purefin.data.SearchManager
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.feature.search.SearchResult
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.Genre
import hu.bbara.purefin.model.MediaKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchManagerImpl @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository
) : SearchManager {

    private suspend fun getServerUrl(): String {
        return userSessionRepository.serverUrl.first()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val searchTerm = MutableStateFlow("")
    private val selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    private val _genres = MutableStateFlow<Set<Genre>>(emptySet())

    override val searchResult: StateFlow<List<SearchResult>> =
        combine(searchTerm, selectedGenres) { searchTerm, genres ->
            search(searchTerm, genres)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val genres: StateFlow<Set<Genre>> = _genres.asStateFlow()

    init {
        scope.launch {
            _genres.value = jellyfinApiClient.getGenres()
                .mapNotNull { it.name }
                .map { Genre(name = it) }
                .toSet()
        }
    }

    override fun setGenres(genres: Set<String>) {
        selectedGenres.value = genres
    }

    override fun setSearchTerm(searchTerm: String) {
        this.searchTerm.value = searchTerm
    }

    private suspend fun search(searchTerm: String, genres: Set<String>): List<SearchResult> {
        if (searchTerm.isNotEmpty()) {
            val searchBySearchTerm = jellyfinApiClient.searchBySearchTerm(searchTerm)
            return searchBySearchTerm.map { it.toSearchResult() }
        }
        if (genres.isNotEmpty()) {
            val searchByGenre = jellyfinApiClient.searchByGenre(genres)
            return searchByGenre.map { it.toSearchResult() }
        }
        return emptyList()
    }

    private suspend fun BaseItemDto.toSearchResult(): SearchResult {
        return SearchResult(
            id = id,
            title = name!!,
            posterUrl = ImageUrlBuilder.toImageUrl(
                url = getServerUrl(),
                itemId = id,
                artworkKind = ArtworkKind.PRIMARY
            ),
            type = when (type) {
                BaseItemKind.MOVIE -> MediaKind.MOVIE
                BaseItemKind.SERIES -> MediaKind.SERIES
                else -> throw UnsupportedOperationException("Unsupported media type: $type")
            }
        )
    }


}
