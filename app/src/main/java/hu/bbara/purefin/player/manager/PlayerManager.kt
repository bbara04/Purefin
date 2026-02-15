package hu.bbara.purefin.player.manager

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.player.model.QueueItemUi
import hu.bbara.purefin.player.model.TrackOption
import hu.bbara.purefin.player.model.TrackType
import hu.bbara.purefin.player.preference.AudioTrackProperties
import hu.bbara.purefin.player.preference.SubtitleTrackProperties
import hu.bbara.purefin.player.preference.TrackMatcher
import hu.bbara.purefin.player.preference.TrackPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Encapsulates the Media3 [Player] wiring and exposes reactive updates for the UI layer.
 */
@ViewModelScoped
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    val player: Player,
    private val trackMapper: TrackMapper,
    private val trackPreferencesRepository: TrackPreferencesRepository,
    private val trackMatcher: TrackMatcher
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentMediaContext: MediaContext? = null

    private val _playbackState = MutableStateFlow(PlaybackStateSnapshot())
    val playbackState: StateFlow<PlaybackStateSnapshot> = _playbackState.asStateFlow()

    private val _progress = MutableStateFlow(PlaybackProgressSnapshot())
    val progress: StateFlow<PlaybackProgressSnapshot> = _progress.asStateFlow()

    private val _metadata = MutableStateFlow(MetadataState())
    val metadata: StateFlow<MetadataState> = _metadata.asStateFlow()

    private val _tracks = MutableStateFlow(TrackSelectionState())
    val tracks: StateFlow<TrackSelectionState> = _tracks.asStateFlow()

    private val _queue = MutableStateFlow<List<QueueItemUi>>(emptyList())
    val queue: StateFlow<List<QueueItemUi>> = _queue.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying, isBuffering = false, isEnded = false) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val buffering = playbackState == Player.STATE_BUFFERING
            val ended = playbackState == Player.STATE_ENDED
            _playbackState.update { state ->
                state.copy(
                    isBuffering = buffering,
                    isEnded = ended,
                    error = if (playbackState == Player.STATE_IDLE) state.error else null
                )
            }
            if (ended) player.pause()
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update { it.copy(error = error.errorCodeName ?: error.localizedMessage ?: "Playback error") }
        }

        override fun onTracksChanged(tracks: Tracks) {
            refreshTracks(tracks)
            scope.launch {
                applyTrackPreferences()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            refreshMetadata(mediaItem)
            refreshQueue()
        }
    }

    init {
        player.addListener(listener)
        refreshMetadata(player.currentMediaItem)
        refreshTracks(player.currentTracks)
        refreshQueue()
        startProgressLoop()
    }

    fun play(mediaItem: MediaItem, mediaContext: MediaContext? = null) {
        currentMediaContext = mediaContext
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        refreshMetadata(mediaItem)
        refreshQueue()
        _playbackState.update { it.copy(isEnded = false, error = null) }
    }

    fun addToQueue(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
        refreshQueue()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
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

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
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
        refreshTracks(player.currentTracks)

        // Save track preference if media context is available
        currentMediaContext?.let { context ->
            scope.launch {
                saveTrackPreference(option, context.preferenceKey)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun retry() {
        player.prepare()
        player.playWhenReady = true
    }

    fun playQueueItem(id: String) {
        val items = _queue.value
        val targetIndex = items.indexOfFirst { it.id == id }
        if (targetIndex >= 0) {
            player.seekToDefaultPosition(targetIndex)
            player.playWhenReady = true
            refreshQueue()
        }
    }

    fun clearError() {
        _playbackState.update { it.copy(error = null) }
    }

    private suspend fun applyTrackPreferences() {
        val context = currentMediaContext ?: return
        val preferences = trackPreferencesRepository.getMediaPreferences(context.preferenceKey).firstOrNull() ?: return

        val currentTrackState = _tracks.value

        // Apply audio preference
        preferences.audioPreference?.let { audioPreference ->
            val matchedAudio = trackMatcher.findBestAudioMatch(
                currentTrackState.audioTracks,
                audioPreference
            )
            matchedAudio?.let { selectTrack(it) }
        }

        // Apply subtitle preference
        preferences.subtitlePreference?.let { subtitlePreference ->
            val matchedSubtitle = trackMatcher.findBestSubtitleMatch(
                currentTrackState.textTracks,
                subtitlePreference
            )
            matchedSubtitle?.let { selectTrack(it) }
        }
    }

    private suspend fun saveTrackPreference(option: TrackOption, preferenceKey: String) {
        when (option.type) {
            TrackType.AUDIO -> {
                val properties = AudioTrackProperties(
                    language = option.language,
                    channelCount = option.channelCount,
                    label = option.label
                )
                trackPreferencesRepository.saveAudioPreference(preferenceKey, properties)
            }

            TrackType.TEXT -> {
                val properties = SubtitleTrackProperties(
                    language = option.language,
                    forced = option.forced,
                    label = option.label,
                    isOff = option.isOff
                )
                trackPreferencesRepository.saveSubtitlePreference(preferenceKey, properties)
            }

            TrackType.VIDEO -> {
                // Video preferences not implemented in this feature
            }
        }
    }

    fun release() {
        scope.cancel()
        player.removeListener(listener)
        player.release()
    }

    private fun startProgressLoop() {
        scope.launch {
            while (isActive) {
                val duration = player.duration.takeIf { it > 0 } ?: _progress.value.durationMs
                val position = player.currentPosition
                val buffered = player.bufferedPosition
                _progress.value = PlaybackProgressSnapshot(
                    durationMs = duration,
                    positionMs = position,
                    bufferedMs = buffered,
                    isLive = player.isCurrentMediaItemLive
                )
                delay(500)
            }
        }
    }

    private fun refreshQueue() {
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
        _queue.value = items
    }

    private fun refreshMetadata(mediaItem: MediaItem?) {
        _metadata.value = MetadataState(
            mediaId = mediaItem?.mediaId,
            title = mediaItem?.mediaMetadata?.title?.toString(),
            subtitle = mediaItem?.mediaMetadata?.subtitle?.toString()
        )
    }

    private fun refreshTracks(tracks: Tracks) {
        _tracks.value = trackMapper.map(tracks)
    }
}

data class PlaybackStateSnapshot(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val error: String? = null
)

data class PlaybackProgressSnapshot(
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val isLive: Boolean = false
)

data class MetadataState(
    val mediaId: String? = null,
    val title: String? = null,
    val subtitle: String? = null
)

data class MediaContext(
    val mediaId: String,
    val preferenceKey: String
)
