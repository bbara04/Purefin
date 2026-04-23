package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.data.PlaybackMethod

fun playbackCustomCacheKey(
    mediaId: String,
    playbackUrl: String,
    playMethod: PlaybackMethod
): String? {
    val normalizedUrl = playbackUrl.substringBefore('?').substringBefore('#').lowercase()
    val isAdaptiveManifest =
        normalizedUrl.endsWith(".m3u8") ||
            normalizedUrl.endsWith(".mpd") ||
            normalizedUrl.endsWith(".ism") ||
            normalizedUrl.contains(".ism/")

    return if (playMethod == PlaybackMethod.DIRECT_PLAY && !isAdaptiveManifest) {
        mediaId
    } else {
        null
    }
}
