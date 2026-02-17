package hu.bbara.purefin.app.content.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.download.DownloadState
import hu.bbara.purefin.download.MediaDownloadManager
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ImageType
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MovieScreenViewModel @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val navigationManager: NavigationManager,
    private val userSessionRepository: UserSessionRepository,
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
            val movieInfo = jellyfinApiClient.getItemInfo(movieId)
            if (movieInfo == null) {
                _movie.value = null
                return@launch
            }
            val serverUrl = userSessionRepository.serverUrl.first().trim().ifBlank {
                "https://jellyfin.bbara.hu"
            }
            _movie.value = movieInfo.toUiModel(serverUrl)

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

    private fun BaseItemDto.toUiModel(serverUrl: String): MovieUiModel {
        val year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty()
        val rating = officialRating ?: "NR"
        val runtime = formatRuntime(runTimeTicks)
        val format = container?.uppercase() ?: "VIDEO"
        val synopsis = overview ?: "No synopsis available."
        val heroImageUrl = id?.let { itemId ->
            JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = itemId,
                type = ImageType.BACKDROP
            )
        } ?: ""
        val cast = people.orEmpty().map { it.toCastMember() }
        return MovieUiModel(
            id = id,
            
            title = name ?: "Unknown title",
            year = year,
            rating = rating,
            runtime = runtime,
            format = format,
            synopsis = synopsis,
            heroImageUrl = heroImageUrl,
            audioTrack = "Default",
            subtitles = "Unknown",
            cast = cast
        )
    }

    private fun BaseItemPerson.toCastMember(): CastMember {
        return CastMember(
            name = name ?: "Unknown",
            role = role ?: "",
            imageUrl = null
        )
    }

    private fun formatRuntime(ticks: Long?): String {
        if (ticks == null || ticks <= 0) return "—"
        val totalSeconds = ticks / 10_000_000
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

}
