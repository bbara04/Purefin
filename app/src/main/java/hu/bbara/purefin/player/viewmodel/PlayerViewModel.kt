package hu.bbara.purefin.player.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.player.data.PlayerMediaRepository
import hu.bbara.purefin.data.MediaRepository
import hu.bbara.purefin.player.manager.MediaContext
import hu.bbara.purefin.player.manager.PlayerManager
import hu.bbara.purefin.player.manager.ProgressManager
import hu.bbara.purefin.player.model.PlayerUiState
import hu.bbara.purefin.player.model.TrackOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager,
    private val playerMediaRepository: PlayerMediaRepository,
    private val mediaRepository: MediaRepository,
    private val progressManager: ProgressManager
) : ViewModel() {

    val player get() = playerManager.player

    private val mediaId: String? = savedStateHandle["MEDIA_ID"]

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private var autoHideJob: Job? = null
    private var lastNextUpMediaId: String? = null
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
                if (state.isPlaying) {
                    scheduleAutoHide()
                } else {
                    showControls()
                }
                if (state.isEnded || state.isBuffering) {
                    showControls()
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
                val currentMediaId = metadata.mediaId
                if (!currentMediaId.isNullOrEmpty() && currentMediaId != lastNextUpMediaId) {
                    lastNextUpMediaId = currentMediaId
                    loadNextUp(currentMediaId)
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
            playerManager.queue.collect { queue ->
                _uiState.update { it.copy(queue = queue) }
            }
        }
    }

    private fun loadInitialMedia() {
        val id = mediaId ?: return
        val uuid = id.toUuidOrNull()
        if (uuid == null) {
            dataErrorMessage = "Invalid media id"
            _uiState.update { it.copy(error = dataErrorMessage) }
            return
        }
        viewModelScope.launch {
            val result = playerMediaRepository.getMediaItem(uuid)
            if (result != null) {
                val (mediaItem, resumePositionMs) = result

                // Determine preference key: movies use their own ID, episodes use series ID
                val preferenceKey = mediaRepository.episodes.value[uuid]?.seriesId?.toString() ?: id
                val mediaContext = MediaContext(mediaId = id, preferenceKey = preferenceKey)

                playerManager.play(mediaItem, mediaContext)

                // Seek to resume position after play() is called
                resumePositionMs?.let { playerManager.seekTo(it) }

                if (dataErrorMessage != null) {
                    dataErrorMessage = null
                    _uiState.update { it.copy(error = null) }
                }
            } else {
                dataErrorMessage = "Unable to load media"
                _uiState.update { it.copy(error = dataErrorMessage) }
            }
        }
    }

    private fun loadNextUp(currentMediaId: String) {
        val uuid = currentMediaId.toUuidOrNull() ?: return
        viewModelScope.launch {
            val queuedIds = uiState.value.queue.map { it.id }.toSet()
            val items = playerMediaRepository.getNextUpMediaItems(
                episodeId = uuid,
                existingIds = queuedIds
            )
            items.forEach { playerManager.addToQueue(it) }
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
        scheduleAutoHide()
    }

    fun seekBy(deltaMs: Long) {
        playerManager.seekBy(deltaMs)
        scheduleAutoHide()
    }

    fun seekToLiveEdge() {
        playerManager.seekToLiveEdge()
    }

    fun showControls() {
        _controlsVisible.value = true
        scheduleAutoHide()
    }

    fun toggleControlsVisibility() {
        _controlsVisible.value = !_controlsVisible.value
        if (_controlsVisible.value) scheduleAutoHide()
    }

    private fun scheduleAutoHide() {
        autoHideJob?.cancel()
        if (!player.isPlaying) return
        autoHideJob = viewModelScope.launch {
            delay(3500)
            _controlsVisible.value = false
        }
    }

    fun next() {
        playerManager.next()
        showControls()
    }

    fun previous() {
        playerManager.previous()
        showControls()
    }

    fun selectTrack(option: TrackOption) {
        playerManager.selectTrack(option)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun retry() {
        playerManager.retry()
    }

    fun playQueueItem(id: String) {
        playerManager.playQueueItem(id)
        showControls()
    }

    fun clearError() {
        dataErrorMessage = null
        playerManager.clearError()
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoHideJob?.cancel()
        progressManager.release()
        playerManager.release()
    }

    private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
}
