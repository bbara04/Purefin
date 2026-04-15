package hu.bbara.purefin.feature.browse.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.model.LibraryKind
import hu.bbara.purefin.core.model.MediaKind
import hu.bbara.purefin.feature.browse.home.PosterItem
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    private val selectedLibrary = MutableStateFlow<UUID?>(null)

    val contents: StateFlow<List<PosterItem>> = combine(selectedLibrary, homeRepository.libraries) {
        libraryId, libraries ->
        if (libraryId == null) {
            return@combine emptyList()
        }
        val library = libraries.find { it.id == libraryId } ?: return@combine emptyList()
        when (library.type) {
            LibraryKind.SERIES -> library.series!!.map { series ->
                PosterItem(type = MediaKind.SERIES, series = series)
            }
            LibraryKind.MOVIES -> library.movies!!.map { movie ->
                PosterItem(type = MediaKind.MOVIE, movie = movie)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { homeRepository.ensureReady() }
    }

    fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(Route.MovieRoute(
            MovieDto(
                id = movieId,
            )
        ))
    }

    fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(
                    id = seriesId,
                )
            ))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectLibrary(libraryId: UUID) {
        selectedLibrary.value = libraryId
    }
}
