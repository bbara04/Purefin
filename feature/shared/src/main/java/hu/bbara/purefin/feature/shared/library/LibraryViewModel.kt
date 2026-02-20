package hu.bbara.purefin.feature.shared.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.data.navigation.MovieDto
import hu.bbara.purefin.core.data.navigation.NavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.data.navigation.SeriesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val selectedLibrary = MutableStateFlow<UUID?>(null)

    val contents: StateFlow<List<PosterItem>> = combine(selectedLibrary, mediaRepository.libraries) {
        libraryId, libraries ->
        if (libraryId == null) {
            return@combine emptyList()
        }
        val library = libraries.find { it.id == libraryId } ?: return@combine emptyList()
        when (library.type) {
            CollectionType.TVSHOWS -> library.series!!.map { series ->
                PosterItem(type = BaseItemKind.SERIES, series = series)
            }
            CollectionType.MOVIES -> library.movies!!.map { movie ->
                PosterItem(type = BaseItemKind.MOVIE, movie = movie)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { mediaRepository.ensureReady() }
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
        viewModelScope.launch {
            selectedLibrary.value = libraryId
        }
    }
}
