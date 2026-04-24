package hu.bbara.purefin.feature.content.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.data.MediaCatalogReader
import hu.bbara.purefin.download.DownloadState
import hu.bbara.purefin.download.MediaDownloadController
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieScreenViewModel @Inject constructor(
    private val mediaCatalogReader: MediaCatalogReader,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
): ViewModel() {

    private val _movieId = MutableStateFlow<UUID?>(null)

    val movie: StateFlow<Movie?> = combine(
        _movieId,
        mediaCatalogReader.movies
    ) { movieId, movies ->
        movieId?.let { movies[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun onBack() {
        navigationManager.pop()
    }

    fun onPlay() {
        val id = movie.value?.id?.toString() ?: return
        navigationManager.navigate(Route.PlayerRoute(mediaId = id))
    }

    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun selectMovie(movieId: UUID) {
        _movieId.value = movieId
        viewModelScope.launch {
            mediaDownloadManager.observeDownloadState(movieId.toString()).collect {
                _downloadState.value = it
            }
        }
    }

    fun onDownloadClick() {
        val movieId = movie.value?.id ?: return
        viewModelScope.launch {
            when (_downloadState.value) {
                is DownloadState.NotDownloaded, is DownloadState.Failed -> {
                    mediaDownloadManager.downloadMovie(movieId)
                }
                is DownloadState.Downloading -> {
                    mediaDownloadManager.cancelDownload(movieId)
                }
                is DownloadState.Downloaded -> {
                    mediaDownloadManager.cancelDownload(movieId)
                }
            }
        }
    }

}
