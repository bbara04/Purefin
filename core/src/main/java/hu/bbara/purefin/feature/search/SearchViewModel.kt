package hu.bbara.purefin.feature.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.SearchManager
import hu.bbara.purefin.model.MediaKind
import hu.bbara.purefin.navigation.MovieDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.SeriesDto
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManager: SearchManager,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    val genres = searchManager.genres
    val searchResult = searchManager.searchResult

    fun search(query: String) {
        searchManager.setSearchTerm(query)
    }

    fun setSelectedGenre(genreName: String?) {
        searchManager.setGenres(genreName?.let { setOf(it) } ?: emptySet())
    }

    fun onBack() {
        navigationManager.pop()
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

}
