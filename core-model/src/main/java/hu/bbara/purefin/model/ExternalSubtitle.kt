package hu.bbara.purefin.model

/**
 * Describes an external (sidecar) subtitle stream served by the media server.
 *
 * Used as a transport-layer-agnostic description of a subtitle that must be
 * fetched separately from the primary media stream. The [remoteUrl] is the
 * absolute URL of the subtitle file on the server.
 */
data class ExternalSubtitle(
    val index: Int,
    val language: String?,
    val label: String?,
    val mimeType: String,
    val forced: Boolean,
    val defaultTrack: Boolean,
    val remoteUrl: String,
)

/**
 * Describes an external subtitle that has been downloaded to local storage.
 *
 * [localFilePath] is the absolute path to the downloaded subtitle file. The
 * other fields mirror [ExternalSubtitle] so the player can build a
 * [androidx.media3.common.MediaItem.SubtitleConfiguration] pointing at the
 * local file.
 */
data class DownloadedSubtitle(
    val index: Int,
    val language: String?,
    val label: String?,
    val mimeType: String,
    val forced: Boolean,
    val defaultTrack: Boolean,
    val localFilePath: String,
)