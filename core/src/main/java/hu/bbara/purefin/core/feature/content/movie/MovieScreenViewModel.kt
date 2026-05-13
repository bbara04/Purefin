package hu.bbara.purefin.core.feature.content.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.model.Movie
import java.util.UUID
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
    private val mediaCatalogReader: LocalMediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
): ViewModel() {

    private val _movieId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val movie: StateFlow<Movie?> = _movieId
        .flatMapLatest { movieId ->
            if (movieId == null) flowOf(null) else mediaCatalogReader.getMovie(movieId)
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
