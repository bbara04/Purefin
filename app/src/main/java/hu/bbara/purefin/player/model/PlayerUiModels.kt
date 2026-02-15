package hu.bbara.purefin.player.model

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val isLive: Boolean = false,
    val title: String? = null,
    val subtitle: String? = null,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val error: String? = null,
    val playbackSpeed: Float = 1f,
    val chapters: List<TimedMarker> = emptyList(),
    val ads: List<TimedMarker> = emptyList(),
    val queue: List<QueueItemUi> = emptyList(),
    val audioTracks: List<TrackOption> = emptyList(),
    val textTracks: List<TrackOption> = emptyList(),
    val qualityTracks: List<TrackOption> = emptyList(),
    val selectedAudioTrackId: String? = null,
    val selectedTextTrackId: String? = null,
    val selectedQualityTrackId: String? = null,
)

data class TrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val bitrate: Int?,
    val channelCount: Int?,
    val height: Int?,
    val groupIndex: Int,
    val trackIndex: Int,
    val type: TrackType,
    val isOff: Boolean,
    val forced: Boolean = false
)

enum class TrackType { AUDIO, TEXT, VIDEO }

data class TimedMarker(
    val positionMs: Long,
    val type: MarkerType,
    val label: String? = null
)

enum class MarkerType { CHAPTER, AD }

data class QueueItemUi(
    val id: String,
    val title: String,
    val subtitle: String?,
    val artworkUrl: String?,
    val isCurrent: Boolean
)
