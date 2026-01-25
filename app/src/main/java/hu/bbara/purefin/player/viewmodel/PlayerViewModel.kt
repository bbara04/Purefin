package hu.bbara.purefin.player.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.player.model.PlayerUiState
import hu.bbara.purefin.player.model.QueueItemUi
import hu.bbara.purefin.player.model.TrackOption
import hu.bbara.purefin.player.model.TrackType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.MediaSourceInfo
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val player: Player,
    val jellyfinApiClient: JellyfinApiClient
) : ViewModel() {

    val mediaId: String? = savedStateHandle["MEDIA_ID"]
    private val videoUris = savedStateHandle.getStateFlow("videoUris", emptyList<Uri>())
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private var autoHideJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying, isBuffering = false, isEnded = false) }
            if (isPlaying) {
                scheduleAutoHide()
            } else {
                showControls()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val buffering = playbackState == Player.STATE_BUFFERING
            val ended = playbackState == Player.STATE_ENDED
            _uiState.update { state ->
                state.copy(
                    isBuffering = buffering,
                    isEnded = ended,
                    error = if (playbackState == Player.STATE_IDLE) state.error else null
                )
            }
            if (buffering || ended) showControls()
            if (ended) player.pause()
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update { it.copy(error = error.errorCodeName ?: error.localizedMessage ?: "Playback error") }
            showControls()
        }

        override fun onTracksChanged(tracks: Tracks) {
            updateTracks(tracks)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateMetadata(mediaItem)
            updateQueue()
        }
    }

    init {
        observePlayer()
        loadMedia()
        startProgressUpdates()
    }

    private fun observePlayer() {
        player.addListener(playerListener)
    }

    fun loadMedia() {
        viewModelScope.launch {
            val mediaSources: List<MediaSourceInfo> =
                jellyfinApiClient.getMediaSources(UUID.fromString(mediaId!!))
            val contentUriString =
                jellyfinApiClient.getMediaPlaybackInfo(
                    mediaId = UUID.fromString(mediaId),
                    mediaSourceId = mediaSources.first().id
                )
            contentUriString?.toUri()?.let {
                playVideo(it)
            }
        }
    }

    fun addVideoUri(contentUri: Uri) {
        savedStateHandle["videoUris"] = videoUris.value + contentUri
        player.addMediaItem(MediaItem.fromUri(contentUri))
    }

    fun playVideo(uri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(mediaId ?: uri.toString())
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        updateQueue()
        updateMetadata(mediaItem)
        updateTracks()
        _uiState.update { it.copy(isEnded = false, error = null) }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        scheduleAutoHide()
    }

    fun seekBy(deltaMs: Long) {
        val target = (player.currentPosition + deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun seekToLiveEdge() {
        if (player.isCurrentMediaItemLive) {
            player.seekToDefaultPosition()
            player.play()
        }
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
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            showControls()
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            showControls()
        }
    }

    fun selectTrack(option: TrackOption) {
        val builder = player.trackSelectionParameters.buildUpon()
        when (option.type) {
            TrackType.TEXT -> {
                if (option.isOff) {
                    builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    builder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                } else {
                    builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    builder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
                    val group = player.currentTracks.groups.getOrNull(option.groupIndex) ?: return
                    builder.addOverride(
                        TrackSelectionOverride(group.mediaTrackGroup, listOf(option.trackIndex))
                    )
                }
            }

            TrackType.AUDIO -> {
                builder.clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                val group = player.currentTracks.groups.getOrNull(option.groupIndex) ?: return
                builder.addOverride(
                    TrackSelectionOverride(group.mediaTrackGroup, listOf(option.trackIndex))
                )
            }

            TrackType.VIDEO -> {
                builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                val group = player.currentTracks.groups.getOrNull(option.groupIndex) ?: return
                builder.addOverride(
                    TrackSelectionOverride(group.mediaTrackGroup, listOf(option.trackIndex))
                )
            }
        }
        player.trackSelectionParameters = builder.build()
        updateTracks()
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun retry() {
        player.prepare()
        player.playWhenReady = true
    }

    fun playQueueItem(id: String) {
        val items = _uiState.value.queue
        val targetIndex = items.indexOfFirst { it.id == id }
        if (targetIndex >= 0) {
            player.seekToDefaultPosition(targetIndex)
            player.playWhenReady = true
            showControls()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (isActive) {
                val duration = player.duration.takeIf { it > 0 } ?: _uiState.value.durationMs
                val position = player.currentPosition
                val buffered = player.bufferedPosition
                _uiState.update {
                    it.copy(
                        durationMs = duration,
                        positionMs = position,
                        bufferedMs = buffered,
                        isLive = player.isCurrentMediaItemLive
                    )
                }
                delay(500)
            }
        }
    }

    private fun updateTracks(tracks: Tracks = player.currentTracks) {
        val audio = mutableListOf<TrackOption>()
        val text = mutableListOf<TrackOption>()
        val video = mutableListOf<TrackOption>()
        var selectedAudio: String? = null
        var selectedText: String? = null
        var selectedVideo: String? = null

        tracks.groups.forEachIndexed { groupIndex, group ->
            when (group.type) {
                C.TRACK_TYPE_AUDIO -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "a_${groupIndex}_$trackIndex"
                        val label = format.label
                            ?: format.language
                            ?: "${format.channelCount}ch"
                            ?: "Audio $trackIndex"
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = format.language,
                            bitrate = format.bitrate,
                            channelCount = format.channelCount,
                            height = null,
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.AUDIO,
                            isOff = false
                        )
                        audio.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedAudio = id
                    }
                }

                C.TRACK_TYPE_TEXT -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "t_${groupIndex}_$trackIndex"
                        val label = format.label
                            ?: format.language
                            ?: "Subtitle $trackIndex"
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = format.language,
                            bitrate = null,
                            channelCount = null,
                            height = null,
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.TEXT,
                            isOff = false
                        )
                        text.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedText = id
                    }
                }

                C.TRACK_TYPE_VIDEO -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "v_${groupIndex}_$trackIndex"
                        val res = if (format.height != Format.NO_VALUE) "${format.height}p" else null
                        val label = res ?: format.label ?: "Video $trackIndex"
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = null,
                            bitrate = format.bitrate,
                            channelCount = null,
                            height = format.height.takeIf { it > 0 },
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.VIDEO,
                            isOff = false
                        )
                        video.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedVideo = id
                    }
                }
            }
        }

        if (text.isNotEmpty()) {
            text.add(
                0,
                TrackOption(
                    id = "text_off",
                    label = "Off",
                    language = null,
                    bitrate = null,
                    channelCount = null,
                    height = null,
                    groupIndex = -1,
                    trackIndex = -1,
                    type = TrackType.TEXT,
                    isOff = true
                )
            )
        }

        _uiState.update {
            it.copy(
                audioTracks = audio,
                textTracks = text,
                qualityTracks = video,
                selectedAudioTrackId = selectedAudio,
                selectedTextTrackId = selectedText ?: text.firstOrNull { option -> option.isOff }?.id,
                selectedQualityTrackId = selectedVideo
            )
        }
    }

    private fun updateQueue() {
        val items = mutableListOf<QueueItemUi>()
        for (i in 0 until player.mediaItemCount) {
            val mediaItem = player.getMediaItemAt(i)
            items.add(
                QueueItemUi(
                    id = mediaItem.mediaId.ifEmpty { i.toString() },
                    title = mediaItem.mediaMetadata.title?.toString() ?: "Item ${i + 1}",
                    subtitle = mediaItem.mediaMetadata.subtitle?.toString(),
                    artworkUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
                    isCurrent = i == player.currentMediaItemIndex
                )
            )
        }
        _uiState.update { it.copy(queue = items) }
    }

    private fun updateMetadata(mediaItem: MediaItem?) {
        mediaItem ?: return
        _uiState.update {
            it.copy(
                title = mediaItem.mediaMetadata.title?.toString(),
                subtitle = mediaItem.mediaMetadata.subtitle?.toString()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoHideJob?.cancel()
        player.removeListener(playerListener)
        player.release()
    }
}
