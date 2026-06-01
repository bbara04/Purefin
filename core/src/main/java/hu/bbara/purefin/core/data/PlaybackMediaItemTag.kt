package hu.bbara.purefin.core.data

/**
 * Playback-only data stored in [androidx.media3.common.MediaItem.LocalConfiguration.tag].
 *
 * The player uses [playbackReportContext] for Jellyfin progress reporting and consumes the
 * transcoding fallback fields once when direct playback fails.
 */
data class PlaybackMediaItemTag(
    val playbackReportContext: PlaybackReportContext,
    val transcodingFallbackUrl: String?,
)
