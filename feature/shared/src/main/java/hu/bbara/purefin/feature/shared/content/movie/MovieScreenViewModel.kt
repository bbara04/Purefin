package hu.bbara.purefin.feature.shared.content.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.data.navigation.NavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.download.MediaDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class MovieScreenViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadManager
): ViewModel() {

    private val _movie = MutableStateFlow<MovieUiModel?>(null)
    val movie = _movie.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun onBack() {
        navigationManager.pop()
    }


    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun selectMovie(movieId: UUID) {
        viewModelScope.launch {
            val movieData = mediaRepository.movies.value[movieId]
            if (movieData == null) {
                _movie.value = null
                return@launch
            }
            _movie.value = movieData.toUiModel()

            launch {
                mediaDownloadManager.observeDownloadState(movieId.toString()).collect {
                    _downloadState.value = it
                }
            }
        }
    }

    fun onDownloadClick() {
        val movieId = _movie.value?.id ?: return
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

    private fun Movie.toUiModel(): MovieUiModel {
        return MovieUiModel(
            id = id,
            title = title,
            year = year,
            rating = rating,
            runtime = runtime,
            format = format,
            synopsis = synopsis,
            heroImageUrl = heroImageUrl,
            audioTrack = audioTrack,
            subtitles = subtitles,
            progress = progress,
            cast = cast.map { CastMember(name = it.name, role = it.role, imageUrl = it.imageUrl) }
        )
    }

}
