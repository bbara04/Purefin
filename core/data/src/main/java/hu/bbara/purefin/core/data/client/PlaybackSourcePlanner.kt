package hu.bbara.purefin.core.data.client

import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.PlayMethod

internal object PlaybackSourcePlanner {
    data class Plan(
        val mediaSource: MediaSourceInfo,
        val urlStrategy: UrlStrategy,
        val playMethod: PlayMethod,
        val canRetryWithTranscoding: Boolean,
        val usedFallback: Boolean
    )

    enum class UrlStrategy {
        DIRECT_PLAY,
        DIRECT_STREAM,
        TRANSCODE
    }

    fun plan(mediaSources: List<MediaSourceInfo>, forceTranscode: Boolean): Plan? {
        val selected = when {
            forceTranscode -> mediaSources.firstOrNull { it.supportsTranscoding }?.let { mediaSource ->
                PlannedSelection(
                    mediaSource = mediaSource,
                    urlStrategy = UrlStrategy.TRANSCODE,
                    playMethod = PlayMethod.TRANSCODE
                )
            }

            else -> mediaSources.firstOrNull { it.supportsDirectPlay }?.let { mediaSource ->
                PlannedSelection(
                    mediaSource = mediaSource,
                    urlStrategy = UrlStrategy.DIRECT_PLAY,
                    playMethod = PlayMethod.DIRECT_PLAY
                )
            } ?: mediaSources.firstOrNull { it.supportsDirectStream }?.let { mediaSource ->
                PlannedSelection(
                    mediaSource = mediaSource,
                    urlStrategy = UrlStrategy.DIRECT_STREAM,
                    playMethod = PlayMethod.DIRECT_STREAM
                )
            } ?: mediaSources.firstOrNull { it.supportsTranscoding }?.let { mediaSource ->
                PlannedSelection(
                    mediaSource = mediaSource,
                    urlStrategy = UrlStrategy.TRANSCODE,
                    playMethod = PlayMethod.TRANSCODE
                )
            } ?: mediaSources.firstOrNull()?.let { mediaSource ->
                PlannedSelection(
                    mediaSource = mediaSource,
                    urlStrategy = UrlStrategy.DIRECT_STREAM,
                    playMethod = PlayMethod.DIRECT_STREAM,
                    usedFallback = true
                )
            }
        } ?: return null

        return Plan(
            mediaSource = selected.mediaSource,
            urlStrategy = selected.urlStrategy,
            playMethod = selected.playMethod,
            canRetryWithTranscoding = !forceTranscode &&
                selected.playMethod != PlayMethod.TRANSCODE &&
                mediaSources.any { it.supportsTranscoding },
            usedFallback = selected.usedFallback
        )
    }

    fun plan(mediaSource: MediaSourceInfo): Plan? = plan(listOf(mediaSource), forceTranscode = false)

    private data class PlannedSelection(
        val mediaSource: MediaSourceInfo,
        val urlStrategy: UrlStrategy,
        val playMethod: PlayMethod,
        val usedFallback: Boolean = false
    )
}
