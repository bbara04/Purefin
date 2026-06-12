package hu.bbara.purefin.data.jellyfin

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.PlayableMediaRepository
import hu.bbara.purefin.core.data.PlaybackMediaItemTag
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.player.preference.TrackPreferencesRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.JellyfinPlaybackResolver
import hu.bbara.purefin.data.jellyfin.playback.PlaybackSource
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.MediaSegment
import hu.bbara.purefin.model.PlayableMedia
import hu.bbara.purefin.model.SegmentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaSegmentDto
import org.jellyfin.sdk.model.api.MediaSegmentType.INTRO
import org.jellyfin.sdk.model.api.MediaSegmentType.OUTRO
import org.jellyfin.sdk.model.api.MediaSegmentType.PREVIEW
import org.jellyfin.sdk.model.api.MediaSegmentType.RECAP
import org.jellyfin.sdk.model.api.MediaSourceInfo
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPlayableMediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val jellyfinPlaybackResolver: JellyfinPlaybackResolver,
    private val trackPreferencesRepository: TrackPreferencesRepository,
    private val userSessionRepository: UserSessionRepository,
    private val mediaDownloadController: MediaDownloadController,
    private val networkMonitor: NetworkMonitor,
    @param:Offline private val offlineMediaRepository: LocalMediaRepository,
) : PlayableMediaRepository {

    override suspend fun getPlayableMedia(mediaId: UUID): PlayableMedia? = withContext(Dispatchers.IO) {
        val downloadedMediaItem = mediaDownloadController.getCompletedDownloadMediaItem(mediaId.toString())
        if (downloadedMediaItem != null) {
            return@withContext getDownloadedPlayableMedia(mediaId, downloadedMediaItem)
        }
        getStreamingPlayableMedia(mediaId)
    }

    private suspend fun getDownloadedPlayableMedia(
        mediaId: UUID,
        downloadedMediaItem: MediaItem,
    ): PlayableMedia? {
        if (!networkMonitor.isOnline.first()) {
            return getOfflineDownloadedPlayableMedia(mediaId, downloadedMediaItem)
        }

        val onlinePlayableMedia = runCatching {
            getOnlinePlayableMedia(mediaId, downloadedMediaItem)
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            networkMonitor.checkConnection()
            Timber.tag(TAG).w(error, "Unable to load online metadata for downloaded media $mediaId")
            null
        }
        if (onlinePlayableMedia != null) return onlinePlayableMedia

        networkMonitor.checkConnection()
        return getOfflineDownloadedPlayableMedia(mediaId, downloadedMediaItem)
    }

    private suspend fun getStreamingPlayableMedia(mediaId: UUID): PlayableMedia? {
        if (!networkMonitor.isOnline.first()) return null

        return runCatching {
            getOnlinePlayableMedia(mediaId, downloadedMediaItem = null)
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            networkMonitor.checkConnection()
            Timber.tag(TAG).w(error, "Unable to load streaming media $mediaId")
            null
        }
    }

    private suspend fun getOnlinePlayableMedia(
        mediaId: UUID,
        downloadedMediaItem: MediaItem?,
    ): PlayableMedia? {
        val baseItem = jellyfinApiClient.getItemInfo(mediaId) ?: return null
        val playbackSource = jellyfinPlaybackResolver.getPlaybackSource(mediaId) ?: return null

        val mediaItem = if (downloadedMediaItem == null) {
            getMediaItem(baseItem, playbackSource)
        } else {
            getDownloadedMediaItem(baseItem, downloadedMediaItem)
        }
        val resumePositionMs = calculateResumePosition(baseItem, playbackSource.mediaSource)
        val preferenceMediaId = when (baseItem.type) {
            BaseItemKind.EPISODE -> baseItem.seriesId ?: mediaId
            else -> mediaId
        }
        val mediaTrackPreferences = trackPreferencesRepository.getMediaPreferences(preferenceMediaId.toString()).first()
        val mediaSegments = getMediaSegments(mediaId)
        return when (baseItem.type) {
            BaseItemKind.MOVIE -> PlayableMedia.Movie(
                id = mediaId,
                preferenceMediaId = preferenceMediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            BaseItemKind.SERIES -> PlayableMedia.Series(
                id = mediaId,
                preferenceMediaId = preferenceMediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            BaseItemKind.EPISODE -> PlayableMedia.Episode(
                id = mediaId,
                preferenceMediaId = preferenceMediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            else -> null
        }
    }

    private suspend fun getOfflineDownloadedPlayableMedia(
        mediaId: UUID,
        downloadedMediaItem: MediaItem,
    ): PlayableMedia? {
        offlineMediaRepository.getMovie(mediaId).first()?.let { movie ->
            val preferences = trackPreferencesRepository.getMediaPreferences(mediaId.toString()).first()
            return PlayableMedia.Movie(
                id = mediaId,
                preferenceMediaId = mediaId,
                mediaItem = downloadedMediaItem.withMetadata(
                    mediaId = mediaId.toString(),
                    title = movie.title,
                    subtitle = null,
                    artworkUrl = ImageUrlBuilder.finishImageUrl(movie.imageUrlPrefix, ArtworkKind.PRIMARY),
                    playbackReportContext = null,
                ),
                resumePositionMs = calculateOfflineResumePosition(movie.progress, movie.runtime, movie.watched),
                preferences = preferences,
                mediaSegments = emptyList()
            )
        }

        offlineMediaRepository.getEpisode(mediaId).first()?.let { episode ->
            val preferences = trackPreferencesRepository.getMediaPreferences(episode.seriesId.toString()).first()
            return PlayableMedia.Episode(
                id = mediaId,
                preferenceMediaId = episode.seriesId,
                mediaItem = downloadedMediaItem.withMetadata(
                    mediaId = mediaId.toString(),
                    title = episode.title,
                    subtitle = episode.seasonEpisodeLabel(),
                    artworkUrl = ImageUrlBuilder.finishImageUrl(episode.imageUrlPrefix, ArtworkKind.PRIMARY),
                    playbackReportContext = null,
                ),
                resumePositionMs = calculateOfflineResumePosition(episode.progress, episode.runtime, episode.watched),
                preferences = preferences,
                mediaSegments = emptyList()
            )
        }

        return null
    }

    private suspend fun getMediaItem(
        baseItem: BaseItemDto,
        playbackSource: PlaybackSource,
    ): MediaItem = withContext(Dispatchers.IO) {
        val mediaId = baseItem.id
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = ImageUrlBuilder.toImageUrl(serverUrl, mediaId, ArtworkKind.PRIMARY)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            url = playbackSource.directPlayUrl,
            title = baseItem?.name ?: "Unknown",
            subtitle = seasonEpisodeLabel(baseItem),
            artworkUrl = artworkUrl,
            playbackTag = playbackSource.toPlaybackMediaItemTag(),
        )

        return@withContext mediaItem
    }

    private suspend fun getDownloadedMediaItem(
        baseItem: BaseItemDto,
        downloadedMediaItem: MediaItem,
    ): MediaItem = withContext(Dispatchers.IO) {
        val mediaId = baseItem.id
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)
        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = ImageUrlBuilder.toImageUrl(serverUrl, mediaId, ArtworkKind.PRIMARY)

        return@withContext downloadedMediaItem.withMetadata(
            mediaId = mediaId.toString(),
            title = baseItem?.name ?: "Unknown",
            subtitle = seasonEpisodeLabel(baseItem),
            artworkUrl = artworkUrl,
            // TODO
            playbackReportContext = null,
        )
    }

    private suspend fun getMediaSegments(mediaId: UUID): List<MediaSegment> {
        val mediaSegments = jellyfinApiClient.getMediaSegments(mediaId)
        return mediaSegments.mapNotNull {
            it.toMediaSegment()
        }
    }

    override suspend fun getNextUpPlayableMedias(
        episodeId: UUID,
        existingIds: Set<UUID>,
        count: Int,
    ): List<PlayableMedia> = withContext(Dispatchers.IO) {
        runCatching {
            val episodes = jellyfinApiClient.getNextEpisodes(episodeId = episodeId, count = count)
            episodes.mapNotNull { episode ->
                val id = episode.id ?: return@mapNotNull null
                if (existingIds.contains(id)) {
                    return@mapNotNull null
                }
                getPlayableMedia(id)
            }
        }.getOrElse { error ->
            Timber.tag(TAG).w(error, "Unable to load next-up items for $episodeId")
            emptyList()
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaItem(
        mediaId: String,
        url: String,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        playbackTag: Any?,
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtworkUri(artworkUrl.toUri())
            .build()
        val builder = MediaItem.Builder()
            .setUri(url.toUri())
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .setTag(playbackTag)
        return builder.build()
    }

    private fun PlaybackSource.toPlaybackMediaItemTag(): PlaybackMediaItemTag {
        return PlaybackMediaItemTag(
            playbackReportContext = playbackReportContext,
            transcodingFallbackUrl = transcodingUrl,
        )
    }

    private fun calculateResumePosition(
        baseItem: BaseItemDto?,
        mediaSource: MediaSourceInfo,
    ): Long? {
        val userData = baseItem?.userData ?: return null
        val runtimeTicks = mediaSource.runTimeTicks ?: baseItem.runTimeTicks ?: 0L
        if (runtimeTicks == 0L) return null

        val playbackPositionTicks = userData.playbackPositionTicks ?: 0L
        if (playbackPositionTicks == 0L) return null

        val positionMs = playbackPositionTicks / 10_000
        val percentage = (playbackPositionTicks.toDouble() / runtimeTicks.toDouble()) * 100.0
        return if (percentage in 5.0..95.0) positionMs else null
    }

    private fun seasonEpisodeLabel(item: BaseItemDto?): String? {
        val seasonNumber = item?.parentIndexNumber ?: return null
        val episodeNumber = item.indexNumber ?: return null
        return "S$seasonNumber:E$episodeNumber"
    }

    private fun Episode.seasonEpisodeLabel(): String = "S$seasonIndex:E$index"

    private fun MediaItem.withMetadata(
        mediaId: String,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        playbackReportContext: PlaybackReportContext?,
    ): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
        if (artworkUrl.isNotBlank()) {
            metadataBuilder.setArtworkUri(artworkUrl.toUri())
        }
        return buildUpon()
            .setMediaId(mediaId)
            .setMediaMetadata(metadataBuilder.build())
            .setTag(playbackReportContext)
            .build()
    }

    /**
     * Calculates the resume position for offline media from the stored progress percentage
     * and runtime string. Applies the same 5%-95% threshold logic as [calculateResumePosition].
     */
    private fun calculateOfflineResumePosition(
        progress: Double?,
        runtime: String,
        watched: Boolean,
    ): Long {
        if (watched) return 0L
        val progressPercent = progress ?: return 0L
        if (progressPercent.isNaN() || progressPercent <= 0.0) return 0L

        val runtimeMs = parseRuntimeToMs(runtime) ?: return 0L
        if (runtimeMs <= 0L) return 0L

        val positionMs = ((progressPercent / 100.0) * runtimeMs).toLong()
        return if (progressPercent in 5.0..95.0) positionMs else 0L
    }

    /**
     * Parses a runtime string in the format "2h 1m" or "25m" (produced by [formatRuntime])
     * into milliseconds. Returns null for unparseable values like "—".
     */
    private fun parseRuntimeToMs(runtime: String): Long? {
        if (runtime.isBlank() || runtime == "—") return null
        val regex = Regex("""(?:(\d+)h\s*)?(\d+)m""")
        val match = regex.matchEntire(runtime.trim()) ?: return null
        val hours = match.groupValues[1].toLongOrNull() ?: 0L
        val minutes = match.groupValues[2].toLongOrNull() ?: 0L
        return TimeUnit.MINUTES.toMillis(hours * 60 + minutes)
    }

    private fun MediaSegmentDto.toMediaSegment(): MediaSegment {
        val segmentType = when (type) {
            INTRO -> SegmentType.INTRO
            PREVIEW -> SegmentType.PREVIEW
            RECAP -> SegmentType.RECAP
            OUTRO -> SegmentType.OUTRO
            else -> SegmentType.MAIN_CONTENT
        }
        return MediaSegment(
            id = itemId,
            type = segmentType,
            startMs = startTicks / 10_000L,
            endMs = endTicks / 10_000L
        )
    }

    private companion object {
        const val TAG = "PlayableMediaRepo"
    }
}
