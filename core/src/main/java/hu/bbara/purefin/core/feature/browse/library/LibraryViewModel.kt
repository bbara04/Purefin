package hu.bbara.purefin.core.feature.browse.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.data.MediaMetadataUpdater
import hu.bbara.purefin.core.model.MediaUiModel
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val navigationManager: NavigationManager,
    private val mediaMetadataUpdater: MediaMetadataUpdater,
) : ViewModel() {

    private val selectedLibrary = MutableStateFlow<UUID?>(null)

    // Local cache of the last-fetched content per library. Used to preserve
    // content when re-selecting a library whose ETag matches (so the
    // repository's 304 response can be treated as "use the cached copy").
    private val cachedContents = mutableMapOf<UUID, List<MediaUiModel>>()

    private val _contents = MutableStateFlow<List<MediaUiModel>>(emptyList())
    val contents: StateFlow<List<MediaUiModel>> = _contents.asStateFlow()

    init {
        viewModelScope.launch { homeRepository.ensureReady() }
        viewModelScope.launch {
            selectedLibrary.collect { libraryId ->
                if (libraryId == null) {
                    _contents.value = emptyList()
                } else {
                    val fresh = homeRepository.loadLibraryContent(libraryId)
                    if (fresh != null) {
                        cachedContents[libraryId] = fresh
                        _contents.value = fresh
                    } else {
                        // ETag 304 — keep the cached copy for this library if
                        // we have one. On a first-visit 304 (rare, would
                        // require the library's ETag to be set without any
                        // prior fetch) the screen briefly shows empty.
                        _contents.value = cachedContents[libraryId] ?: emptyList()
                    }
                }
            }
        }
    }

    fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(
            Route.MovieRoute(
                MovieDto(
                    id = movieId,
                )
        ))
    }

    fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(
                Route.SeriesRoute(
                    SeriesDto(
                        id = seriesId,
                    )
            ))
        }
    }

    fun markAsWatched(mediaUiModel: MediaUiModel, watched: Boolean) {
        viewModelScope.launch {
            mediaMetadataUpdater.markAsWatched(mediaUiModel.id, watched)
        }
    }

    fun onBack() {
        navigationManager.pop()
    }

    fun selectLibrary(libraryId: UUID) {
        selectedLibrary.value = libraryId
    }
}
