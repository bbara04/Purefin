package hu.bbara.purefin.core.player.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.player.data.PlayerMediaRepository
import hu.bbara.purefin.core.player.manager.MediaContext
import hu.bbara.purefin.core.player.manager.PlayerManager
import hu.bbara.purefin.core.player.manager.ProgressManager
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TrackOption
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
    companion object {
        private const val DEFAULT_CONTROLS_AUTO_HIDE_MS = 3_500L
    }

    val player get() = playerManager.player

    private val mediaId: String? = savedStateHandle["MEDIA_ID"]

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private val controlsAutoHidePolicy = ControlsAutoHidePolicy(DEFAULT_CONTROLS_AUTO_HIDE_MS)
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
                applyControlsAutoHideCommand(
                    controlsAutoHidePolicy.onPlaybackChanged(state.isPlaying)
                )
                if (state.isEnded) {
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
        loadMediaById(id)
    }

    fun loadMedia(id: String) {
        if (mediaId != null) return // Already loading from SavedStateHandle
        loadMediaById(id)
    }

    private fun loadMediaById(id: String) {
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

    fun togglePlayPause(autoHideDelayMs: Long = DEFAULT_CONTROLS_AUTO_HIDE_MS) {
        playerManager.togglePlayPause()
        showControls(autoHideDelayMs)
    }

    fun pausePlayback() {
        playerManager.pausePlayback()
    }

    fun resumePlayback() {
        playerManager.resumePlayback()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        playerManager.seekBy(deltaMs)
    }

    fun seekToLiveEdge() {
        playerManager.seekToLiveEdge()
    }

    fun setControlsAutoHideDelay(autoHideDelayMs: Long) {
        applyControlsAutoHideCommand(
            controlsAutoHidePolicy.setAutoHideDelay(autoHideDelayMs)
        )
    }

    fun setControlsAutoHideBlocked(
        blocker: ControlsAutoHideBlocker,
        blocked: Boolean
    ) {
        applyControlsAutoHideCommand(
            controlsAutoHidePolicy.setBlocked(blocker, blocked)
        )
    }

    fun showControls(autoHideDelayMs: Long = DEFAULT_CONTROLS_AUTO_HIDE_MS) {
        applyControlsAutoHideCommand(
            controlsAutoHidePolicy.showControls(autoHideDelayMs)
        )
    }

    fun toggleControlsVisibility() {
        applyControlsAutoHideCommand(
            controlsAutoHidePolicy.toggleControlsVisibility()
        )
    }

    private fun applyControlsAutoHideCommand(command: ControlsAutoHideCommand) {
        _controlsVisible.value = controlsAutoHidePolicy.controlsVisible
        autoHideJob?.cancel()
        autoHideJob = null
        if (command !is ControlsAutoHideCommand.Schedule) return
        autoHideJob = viewModelScope.launch {
            delay(command.delayMs)
            controlsAutoHidePolicy.hideControls()
            _controlsVisible.value = controlsAutoHidePolicy.controlsVisible
        }
    }

    fun next(autoHideDelayMs: Long = DEFAULT_CONTROLS_AUTO_HIDE_MS) {
        playerManager.next()
        showControls(autoHideDelayMs)
    }

    fun previous(autoHideDelayMs: Long = DEFAULT_CONTROLS_AUTO_HIDE_MS) {
        playerManager.previous()
        showControls(autoHideDelayMs)
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

    fun playQueueItem(id: String, autoHideDelayMs: Long = DEFAULT_CONTROLS_AUTO_HIDE_MS) {
        playerManager.playQueueItem(id)
        showControls(autoHideDelayMs)
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
