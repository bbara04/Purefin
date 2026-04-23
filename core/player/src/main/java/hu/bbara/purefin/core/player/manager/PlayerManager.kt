package hu.bbara.purefin.core.player.manager

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.model.MediaSegment
import hu.bbara.purefin.core.player.model.MediaContext
import hu.bbara.purefin.core.player.model.MetadataState
import hu.bbara.purefin.core.player.model.PlaybackProgressSnapshot
import hu.bbara.purefin.core.player.model.PlaybackStateSnapshot
import hu.bbara.purefin.core.player.model.QueueItemUi
import hu.bbara.purefin.core.player.model.TrackOption
import hu.bbara.purefin.core.player.model.TrackType
import hu.bbara.purefin.core.player.preference.AudioTrackProperties
import hu.bbara.purefin.core.player.preference.SubtitleTrackProperties
import hu.bbara.purefin.core.player.preference.TrackMatcher
import hu.bbara.purefin.core.player.preference.TrackPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

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
    companion object {
        private const val SEEK_SETTLE_TOLERANCE_MS = 750L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val mediaSegmentManager = MediaSegmentManager(player as ExoPlayer)

    private var currentMediaContext: MediaContext? = null
    private var pendingSeekPositionMs: Long? = null

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
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            syncPendingSeek(newPosition.positionMs)
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update {
                it.copy(error = mapPlayerError(error))
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            clearPendingSeek()
            currentMediaContext?.let {
                installMediaSegments(it.mediaSegments)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            updateFromPlayer(player)
            if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                scope.launch {
                    applyTrackPreferences()
                }
            }
        }
    }

    init {
        player.addListener(listener)
        updateFromPlayer(player)
        startProgressLoop()
    }

    fun play(mediaItem: MediaItem, mediaContext: MediaContext? = null) {
        currentMediaContext = mediaContext
        clearPendingSeek()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun addToQueue(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun pausePlayback() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun resumePlayback() {
        if (!player.isPlaying) {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val target = clampSeekPosition(positionMs)
        pendingSeekPositionMs = target
        _progress.update { progress ->
            progress.copy(
                durationMs = resolveDurationMs(),
                positionMs = target,
                isLive = player.isCurrentMediaItemLive
            )
        }
        player.seekTo(target)
    }

    fun seekBy(deltaMs: Long) {
        val basePosition = pendingSeekPositionMs ?: player.currentPosition
        val target = clampSeekPosition(basePosition + deltaMs)
        seekTo(target)
    }

    fun seekToLiveEdge() {
        clearPendingSeek()
        if (player.isCurrentMediaItemLive) {
            player.seekToDefaultPosition()
            player.play()
        }
    }

    fun next() {
        clearPendingSeek()
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun previous() {
        clearPendingSeek()
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

        // Save track preference if media context is available
        currentMediaContext?.let { context ->
            scope.launch {
                saveTrackPreference(option, context.mediaId)
            }
        }
    }

    private fun installMediaSegments(mediaSegments: List<MediaSegment>) {
        mediaSegmentManager.addMediaSegments(
            mediaSegments = mediaSegments
        )
    }

    fun retry() {
        clearPendingSeek()
        player.prepare()
        player.playWhenReady = true
    }

    fun playQueueItem(id: String) {
        val items = _queue.value
        val targetIndex = items.indexOfFirst { it.id == id }
        if (targetIndex >= 0) {
            clearPendingSeek()
            player.seekToDefaultPosition(targetIndex)
            player.playWhenReady = true
        }
    }

    fun clearError() {
        _playbackState.update { it.copy(error = null) }
    }

    private fun applyTrackPreferences() {
        val context = currentMediaContext ?: return
        val preferences = context.preferences

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
                val actualPosition = player.currentPosition
                syncPendingSeek(actualPosition)
                val position = pendingSeekPositionMs ?: actualPosition
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

    private fun updateFromPlayer(player: Player) {
        val currentMediaItem = player.currentMediaItem
        val playbackState = player.playbackState
        val playbackReportContext = currentMediaItem?.localConfiguration?.tag as? PlaybackReportContext
        val currentMetadata = player.mediaMetadata
        val playerError = player.playerError

        _playbackState.value = PlaybackStateSnapshot(
            isPlaying = player.isPlaying,
            isBuffering = playbackState == Player.STATE_BUFFERING,
            isEnded = playbackState == Player.STATE_ENDED,
            error = mapPlayerError(playerError)
        )
        _metadata.value = MetadataState(
            mediaId = currentMediaItem?.mediaId,
            title = currentMetadata.title?.toString(),
            subtitle = currentMetadata.subtitle?.toString(),
            playbackReportContext = playbackReportContext,
        )
        _tracks.value = trackMapper.map(player.currentTracks)
        _queue.value = buildQueueSnapshot(player)
    }

    private fun buildQueueSnapshot(player: Player): List<QueueItemUi> {
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
        return items
    }

    private fun clampSeekPosition(positionMs: Long): Long {
        val duration = resolveDurationMs()
        return if (duration > 0) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
    }

    private fun resolveDurationMs(): Long {
        return player.duration.takeIf { it > 0 } ?: _progress.value.durationMs
    }

    private fun clearPendingSeek() {
        pendingSeekPositionMs = null
    }

    private fun mapPlayerError(error: PlaybackException?): String? {
        return error?.errorCodeName ?: error?.localizedMessage ?: error?.message
    }

    private fun syncPendingSeek(positionMs: Long) {
        val pendingPosition = pendingSeekPositionMs ?: return
        if (abs(positionMs - pendingPosition) <= SEEK_SETTLE_TOLERANCE_MS) {
            clearPendingSeek()
        }
    }
}
