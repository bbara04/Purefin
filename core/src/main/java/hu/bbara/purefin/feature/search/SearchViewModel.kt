package hu.bbara.purefin.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.MediaRepository
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val mediaCatalogReader: MediaRepository,
    private val userSessionRepository: UserSessionRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    val genres = mediaCatalogReader.genres

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

    fun onSearchResultSelected(searchResult: SearchResult) {
        when (searchResult.type) {
            MediaKind.MOVIE -> onMovieSelected(searchResult.id)
            MediaKind.SERIES -> onSeriesSelected(searchResult.id)
            else -> Unit
        }
    }

    private fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(
            Route.MovieRoute(
                MovieDto(
                    id = movieId,
                )
            )
        )
    }

    private fun onSeriesSelected(seriesId: UUID) {
        navigationManager.navigate(
            Route.SeriesRoute(
                SeriesDto(
                    id = seriesId,
                )
            )
        )
    }

    private suspend fun createImageUrl(id: UUID) : String {
        return ImageUrlBuilder.toImageUrl(userSessionRepository.serverUrl.first(), id,
            ArtworkKind.PRIMARY)
    }
}
