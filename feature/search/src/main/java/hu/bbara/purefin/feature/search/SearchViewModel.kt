package hu.bbara.purefin.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.data.session.UserSessionRepository
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import hu.bbara.purefin.core.image.ArtworkKind
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val mediaCatalogReader: MediaCatalogReader,
    private val userSessionRepository: UserSessionRepository,
) : ViewModel() {

    private val _searchResult = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    private val query = MutableStateFlow("")

    init {
        combine(
            query.debounce(300).distinctUntilChanged(),
            mediaCatalogReader.movies,
            mediaCatalogReader.series
        ) { currentQuery, movies, series ->
            val filteredMovies = movies.filter {
                it.value.title.contains(currentQuery, ignoreCase = true)
            }
            val filteredSeries = series.filter {
                it.value.name.contains(currentQuery, ignoreCase = true)
            }
            _searchResult.value = filteredMovies.values.map {
                SearchResult.create(it, createImageUrl(it.id))
            } + filteredSeries.values.map {
                SearchResult.create(it, createImageUrl(it.id))
            }
        }.launchIn(viewModelScope)
    }

    fun search(query: String) {
        this.query.value = query
    }

    private suspend fun createImageUrl(id: UUID) : String {
        return ImageUrlBuilder.toImageUrl(userSessionRepository.serverUrl.first(), id,
            ArtworkKind.PRIMARY)
    }
}
