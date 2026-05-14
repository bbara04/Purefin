package hu.bbara.purefin.core.feature.content.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieScreenViewModel @Inject constructor(
    private val defaultMediaCatalogReader: LocalMediaRepository,
    @param:Offline private val offlineMediaCatalogReader: LocalMediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
): ViewModel() {

    private val _movie = MutableStateFlow<MovieDto?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val movie: StateFlow<Movie?> = _movie
        .flatMapLatest { movie ->
            if (movie == null) {
                flowOf(null)
            } else {
                mediaCatalogReader(movie.offline).getMovie(movie.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun selectMovie(movie: MovieDto) {
        _movie.value = movie
        viewModelScope.launch {
            mediaDownloadManager.observeDownloadState(movie.id.toString()).collect {
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
                    if (_movie.value?.offline == true) {
                        navigationManager.pop()
                    }
                }
            }
        }
    }

    private fun mediaCatalogReader(offline: Boolean): LocalMediaRepository {
        return if (offline) offlineMediaCatalogReader else defaultMediaCatalogReader
    }
}
