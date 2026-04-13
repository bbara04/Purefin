package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.PlayMethod

fun playbackCustomCacheKey(
    mediaId: String,
    playbackUrl: String,
    playMethod: PlayMethod
): String? {
    val normalizedUrl = playbackUrl.substringBefore('?').substringBefore('#').lowercase()
    val isAdaptiveManifest =
        normalizedUrl.endsWith(".m3u8") ||
            normalizedUrl.endsWith(".mpd") ||
            normalizedUrl.endsWith(".ism") ||
            normalizedUrl.contains(".ism/")

    return if (playMethod == PlayMethod.DIRECT_PLAY && !isAdaptiveManifest) {
        mediaId
    } else {
        null
    }
}
