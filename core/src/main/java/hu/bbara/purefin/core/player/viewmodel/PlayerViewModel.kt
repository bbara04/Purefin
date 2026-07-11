package hu.bbara.purefin.core.player.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.player.manager.PlayerManager
import hu.bbara.purefin.core.player.manager.ProgressManager
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.PlaylistElementUiModel
import hu.bbara.purefin.core.player.model.TrackOption
import hu.bbara.purefin.model.PlayableMedia
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager,
    private val mediaCatalogReader: LocalMediaRepository,
    private val progressManager: ProgressManager,
) : ViewModel() {
    companion object {
        private const val TAG = "PlayerViewModel"
    }

    val player get() = playerManager.player

    private val mediaId: String? = savedStateHandle["MEDIA_ID"]

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val seekByCollector = SeekByCollector(viewModelScope) { deltaMs ->
        playerManager.seekBy(deltaMs)
    }
    private var autoHideJob: Job? = null
    private var dataErrorMessage: String? = null

    init {
        progressManager.bind(
            playerManager.playbackState,
            playerManager.progress,
            playerManager.metadata
        )
        observePlayerState()
        loadInitialMedia()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerManager.playbackState.collect { state ->
                _uiState.update {
                    it.copy(
                        isPlaying = state.isPlaying,
                        isBuffering = state.isBuffering,
                        isEnded = state.isEnded,
                        error = state.error ?: dataErrorMessage
                    )
                }
            }
        }

        viewModelScope.launch {
            playerManager.progress.collect { progress ->
                _uiState.update {
                    it.copy(
                        durationMs = progress.durationMs,
                        positionMs = progress.positionMs,
                        bufferedMs = progress.bufferedMs,
                        isLive = progress.isLive
                    )
                }
            }
        }

        viewModelScope.launch {
            playerManager.metadata.collect { metadata ->
                _uiState.update {
                    it.copy(
                        title = metadata.title,
                        subtitle = metadata.subtitle
                    )
                }
            }
        }

        viewModelScope.launch {
            playerManager.tracks.collect { tracks ->
                _uiState.update {
                    it.copy(
                        audioTracks = tracks.audioTracks,
                        textTracks = tracks.textTracks,
                        qualityTracks = tracks.videoTracks,
                        selectedAudioTrackId = tracks.selectedAudioTrackId,
                        selectedTextTrackId = tracks.selectedTextTrackId,
                        selectedQualityTrackId = tracks.selectedVideoTrackId
                    )
                }
            }
        }

        viewModelScope.launch {
            playerManager.activeSkippableSegment.collect { mediaSegment ->
                _uiState.update {
                    it.copy(
                        activeSkippableSegmentEndMs = mediaSegment?.endMs,
                        activeSkippableSegmentType = mediaSegment?.type
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(playerManager.playlist, playerManager.currentPlayableMedia) { playlist, currentPlayableMedia ->
                playlist.mapNotNull { playableMedia ->
                    playableMedia.toPlaylistElementUiModel(currentPlayableMedia?.id)
                }
            }.collect { queue ->
                val currentIndex = queue.indexOfFirst { it.isCurrent }
                val nextEpisode = if (currentIndex != -1 && currentIndex + 1 < queue.size) {
                    queue[currentIndex + 1]
                } else {
                    null
                }
                _uiState.update { state ->
                    state.copy(
                        queue = queue,
                        nextEpisode = nextEpisode
                    )
                }
            }
        }
    }

    private fun loadInitialMedia() {
        val id = mediaId ?: return
        viewModelScope.launch {
            loadMediaById(id)
        }
    }

    fun loadMedia(id: String) {
        if (mediaId != null) return // Already loading from SavedStateHandle
        viewModelScope.launch {
            loadMediaById(id)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private suspend fun loadMediaById(id: String) {
        val uuid = id.toUuidOrNull()
        if (uuid == null) {
            dataErrorMessage = "Invalid media id"
            _uiState.update { it.copy(error = dataErrorMessage) }
            return
        }
        mediaCatalogReader.loadEpisode(UUID.fromString(id))
        viewModelScope.launch {
            runCatching {
                playerManager.play(uuid)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun pausePlayback() {
        playerManager.pausePlayback()
    }

    fun resumePlayback() {
        playerManager.resumePlayback()
    }

    fun seekTo(positionMs: Long) {
        seekByCollector.clear()
        playerManager.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        seekByCollector.seekBySoon(deltaMs)
    }

    fun seekToLiveEdge() {
        seekByCollector.clear()
        playerManager.seekToLiveEdge()
    }

    fun skipActiveSegment() {
        seekByCollector.clear()
        playerManager.skipActiveSegment()
    }

    fun next() {
        seekByCollector.clear()
        playerManager.next()
    }

    fun previous() {
        seekByCollector.clear()
        playerManager.previous()
    }

    fun selectTrack(option: TrackOption) {
        playerManager.selectTrack(option)
    }

    fun retry() {
        playerManager.retry()
    }

    fun playQueueItem(id: String) {
        seekByCollector.clear()
        playerManager.play(id.toUuidOrNull() ?: return)
    }

    fun clearError() {
        dataErrorMessage = null
        playerManager.clearError()
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun PlayableMedia.toPlaylistElementUiModel(currentMediaId: UUID?): PlaylistElementUiModel? {
        return when (this) {
            is PlayableMedia.Movie -> {
                mediaCatalogReader.loadMovie(id)
                val movie = mediaCatalogReader.getMovie(id).first()
                if (movie == null) {
                    Timber.tag(TAG).e("Movie not found for playlist item: $id")
                    null
                } else {
                    PlaylistElementUiModel(
                        id = movie.id.toString(),
                        title = movie.title,
                        artworkUrl = ImageUrlBuilder.finishImageUrl(
                            prefixImageUrl = movie.imageUrlPrefix,
                            artworkKind = ArtworkKind.PRIMARY
                        ),
                        isCurrent = currentMediaId == movie.id
                    )
                }
            }

            is PlayableMedia.Series -> {
                mediaCatalogReader.loadSeries(id)
                val series = mediaCatalogReader.getSeries(id).first()
                if (series == null) {
                    Timber.tag(TAG).e("Series not found for playlist item: $id")
                    null
                } else {
                    PlaylistElementUiModel(
                        id = series.id.toString(),
                        title = series.name,
                        artworkUrl = ImageUrlBuilder.finishImageUrl(
                            prefixImageUrl = series.imageUrlPrefix,
                            artworkKind = ArtworkKind.PRIMARY
                        ),
                        isCurrent = currentMediaId == series.id
                    )
                }
            }

            is PlayableMedia.Episode -> {
                mediaCatalogReader.loadEpisode(id)
                val episode = mediaCatalogReader.getEpisode(id).first()
                if (episode == null) {
                    Timber.tag(TAG).e("Episode not found for playlist item: $id")
                    null
                } else {
                    PlaylistElementUiModel(
                        id = episode.id.toString(),
                        title = episode.title,
                        artworkUrl = ImageUrlBuilder.finishImageUrl(
                            prefixImageUrl = episode.imageUrlPrefix,
                            artworkKind = ArtworkKind.PRIMARY
                        ),
                        isCurrent = currentMediaId == episode.id
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoHideJob?.cancel()
        progressManager.release()
        playerManager.release()
    }

    private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
}
