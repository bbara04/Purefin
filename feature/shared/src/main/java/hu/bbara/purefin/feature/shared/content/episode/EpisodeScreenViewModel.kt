package hu.bbara.purefin.feature.shared.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.AppContentRepository
import hu.bbara.purefin.core.data.navigation.NavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.download.MediaDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val appContentRepository: AppContentRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadManager,
): ViewModel() {

    private val _episodeId = MutableStateFlow<UUID?>(null)
    private val _seriesId = MutableStateFlow<UUID?>(null)

    val episode: StateFlow<Episode?> = combine(
        _episodeId,
        appContentRepository.episodes
    ) { id, episodesMap ->
        id?.let { episodesMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val seriesTitle: StateFlow<String?> = combine(
        _seriesId,
        appContentRepository.series
    ) { seriesId, seriesMap ->
        seriesId?.let { id -> seriesMap[id]?.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun onBack() {
        navigationManager.pop()
    }

    fun onPlay() {
        val id = _episodeId.value?.toString() ?: return
        navigationManager.navigate(Route.PlayerRoute(mediaId = id))
    }

    fun onPlaybackStarted() {
        val seriesId = _seriesId.value ?: return
        navigationManager.pop()
        navigationManager.navigate(Route.SeriesRoute(SeriesDto(id = seriesId)))
    }

    fun onSeriesClick() {
        val seriesId = _seriesId.value ?: return
        navigationManager.navigate(Route.SeriesRoute(SeriesDto(id = seriesId)))
    }

    fun selectEpisode(seriesId: UUID, seasonId: UUID, episodeId: UUID) {
        _episodeId.value = episodeId
        _seriesId.value = seriesId
        viewModelScope.launch {
            mediaDownloadManager.observeDownloadState(episodeId.toString()).collect {
                _downloadState.value = it
            }
        }
    }

    fun onDownloadClick() {
        val episodeId = _episodeId.value ?: return
        viewModelScope.launch {
            when (_downloadState.value) {
                is DownloadState.NotDownloaded, is DownloadState.Failed -> {
                    mediaDownloadManager.downloadEpisode(episodeId)
                }
                is DownloadState.Downloading, is DownloadState.Downloaded -> {
                    mediaDownloadManager.cancelEpisodeDownload(episodeId)
                }
            }
        }
    }

}
