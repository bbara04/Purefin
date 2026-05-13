package hu.bbara.purefin.core.feature.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.model.Episode
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val mediaCatalogReader: LocalMediaRepository,
    private val navigationManager: NavigationManager,
    private val mediaDownloadManager: MediaDownloadController,
): ViewModel() {

    private val _episodeId = MutableStateFlow<UUID?>(null)
    private val _seriesId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val episode: StateFlow<Episode?> = _episodeId
        .flatMapLatest { episodeId ->
            if (episodeId == null) flowOf(null) else mediaCatalogReader.getEpisode(episodeId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val seriesTitle: StateFlow<String?> = _seriesId
        .flatMapLatest { seriesId ->
            if (seriesId == null) {
                flowOf(null)
            } else {
                mediaCatalogReader.getSeries(seriesId).map { it?.name }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    fun onBack() {
        navigationManager.pop()
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
