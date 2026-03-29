package hu.bbara.purefin.core.player.viewmodel

import androidx.media3.common.PlaybackException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.MediaRepository
import hu.bbara.purefin.core.data.client.PlaybackReportContext
import hu.bbara.purefin.core.player.data.PlayerMediaRepository
import hu.bbara.purefin.core.player.manager.MediaContext
import hu.bbara.purefin.core.player.manager.PlaybackStateSnapshot
import hu.bbara.purefin.core.player.manager.PlayerManager
import hu.bbara.purefin.core.player.manager.ProgressManager
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TrackOption
import org.jellyfin.sdk.model.api.PlayMethod
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
    private var activeMediaId: String? = null
    private var transcodingRetryMediaId: String? = null

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
                if (state.error != null && maybeRetryWithTranscoding(state)) {
                    _uiState.update { it.copy(isBuffering = true, error = null) }
                    return@collect
                }
                _uiState.update {
                    it.copy(
                        isPlaying = state.isPlaying,
                        isBuffering = state.isBuffering,
                        isEnded = state.isEnded,
                        error = state.error ?: dataErrorMessage
                    )
                }
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
                if (currentMediaId != activeMediaId) {
                    activeMediaId = currentMediaId
                    transcodingRetryMediaId = null
                }
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
        loadMediaById(id = id, forceTranscode = false, startPositionMsOverride = null, replaceCurrent = false)
    }

    private fun loadMediaById(
        id: String,
        forceTranscode: Boolean,
        startPositionMsOverride: Long?,
        replaceCurrent: Boolean
    ) {
        val uuid = id.toUuidOrNull()
        if (uuid == null) {
            dataErrorMessage = "Invalid media id"
            _uiState.update { it.copy(error = dataErrorMessage) }
            return
        }
        viewModelScope.launch {
            val result = playerMediaRepository.getMediaItem(uuid, forceTranscode = forceTranscode)
            if (result != null) {
                val (mediaItem, resumePositionMs) = result

                // Determine preference key: movies use their own ID, episodes use series ID
                val preferenceKey = mediaRepository.episodes.value[uuid]?.seriesId?.toString() ?: id
                val mediaContext = MediaContext(mediaId = id, preferenceKey = preferenceKey)
                val startPositionMs = startPositionMsOverride ?: resumePositionMs

                if (replaceCurrent) {
                    playerManager.replaceCurrentMediaItem(mediaItem, mediaContext, startPositionMs)
                } else {
                    playerManager.play(mediaItem, mediaContext, startPositionMs)
                }

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

    private fun maybeRetryWithTranscoding(state: PlaybackStateSnapshot): Boolean {
        val currentMediaId = playerManager.metadata.value.mediaId ?: return false
        val playbackReportContext = playerManager.metadata.value.playbackReportContext ?: return false
        val errorCode = state.errorCode ?: return false

        if (currentMediaId == transcodingRetryMediaId) return false
        if (!playbackReportContext.canRetryWithTranscoding) return false
        if (!isRetryablePlaybackError(errorCode, state.error, playbackReportContext)) return false

        transcodingRetryMediaId = currentMediaId
        loadMediaById(
            id = currentMediaId,
            forceTranscode = true,
            startPositionMsOverride = player.currentPosition.takeIf { it > 0L },
            replaceCurrent = true
        )
        return true
    }

    private fun isRetryablePlaybackError(
        errorCode: Int,
        errorMessage: String?,
        playbackReportContext: PlaybackReportContext
    ): Boolean {
        if (playbackReportContext.playMethod == PlayMethod.TRANSCODE) {
            return false
        }

        if (errorCode in setOf(
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED
            )
        ) {
            return true
        }

        val message = errorMessage?.lowercase().orEmpty()
        return "decoder" in message || "codec" in message || "unsupported" in message
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
        showControls()
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

    fun showControls() {
        _controlsVisible.value = true
        scheduleAutoHide()
    }

    fun toggleControlsVisibility() {
        _controlsVisible.value = !_controlsVisible.value
        if (_controlsVisible.value) scheduleAutoHide()
    }

    fun hideControls() {
        _controlsVisible.value = false
        autoHideJob?.cancel()
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
        progressManager.syncProgress(playerManager.snapshotProgress())
        progressManager.release()
        playerManager.release()
    }

    private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
}
