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
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.player.preference.TrackPreferencesRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.JellyfinPlaybackResolver
import hu.bbara.purefin.data.jellyfin.playback.PlaybackDecision
import hu.bbara.purefin.data.jellyfin.playback.playbackCustomCacheKey
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

        return runCatching {
            getOnlinePlayableMedia(mediaId, downloadedMediaItem)
                ?: getOfflineDownloadedPlayableMedia(mediaId, downloadedMediaItem)
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            Timber.tag(TAG).w(error, "Unable to load online metadata for downloaded media $mediaId")
            getOfflineDownloadedPlayableMedia(mediaId, downloadedMediaItem)
        }
    }

    private suspend fun getStreamingPlayableMedia(mediaId: UUID): PlayableMedia? {
        return getOnlinePlayableMedia(mediaId, downloadedMediaItem = null)
    }

    private suspend fun getOnlinePlayableMedia(
        mediaId: UUID,
        downloadedMediaItem: MediaItem?,
    ): PlayableMedia? {
        val baseItem = jellyfinApiClient.getItemInfo(mediaId) ?: return null
        val playbackDecision = jellyfinPlaybackResolver.getPlaybackDecision(mediaId) ?: return null

        val mediaItem = if (downloadedMediaItem == null) {
            getMediaItem(baseItem, playbackDecision)
        } else {
            getDownloadedMediaItem(baseItem, playbackDecision, downloadedMediaItem)
        }
        val resumePositionMs = calculateResumePosition(baseItem, playbackDecision.mediaSource)
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
                resumePositionMs = 0L,
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
                resumePositionMs = 0L,
                preferences = preferences,
                mediaSegments = emptyList()
            )
        }

        return null
    }

    private suspend fun getMediaItem(baseItem: BaseItemDto, playbackDecision: PlaybackDecision): MediaItem = withContext(Dispatchers.IO) {
        val mediaId = baseItem.id
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)

        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = ImageUrlBuilder.toImageUrl(serverUrl, mediaId, ArtworkKind.PRIMARY)

        val mediaItem = createMediaItem(
            mediaId = mediaId.toString(),
            playbackDecision = playbackDecision,
            title = baseItem?.name ?: playbackDecision.mediaSource.name ?: "Unknown",
            subtitle = seasonEpisodeLabel(baseItem),
            artworkUrl = artworkUrl,
            playbackReportContext = playbackDecision.reportContext,
        )

        return@withContext mediaItem
    }

    private suspend fun getDownloadedMediaItem(
        baseItem: BaseItemDto,
        playbackDecision: PlaybackDecision,
        downloadedMediaItem: MediaItem,
    ): MediaItem = withContext(Dispatchers.IO) {
        val mediaId = baseItem.id
        val baseItem = jellyfinApiClient.getItemInfo(mediaId)
        val serverUrl = userSessionRepository.serverUrl.first()
        val artworkUrl = ImageUrlBuilder.toImageUrl(serverUrl, mediaId, ArtworkKind.PRIMARY)

        return@withContext downloadedMediaItem.withMetadata(
            mediaId = mediaId.toString(),
            title = baseItem?.name ?: playbackDecision.mediaSource.name ?: "Unknown",
            subtitle = seasonEpisodeLabel(baseItem),
            artworkUrl = artworkUrl,
            playbackReportContext = playbackDecision.reportContext,
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
        playbackDecision: PlaybackDecision,
        title: String,
        subtitle: String?,
        artworkUrl: String,
        playbackReportContext: PlaybackReportContext?,
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setArtworkUri(artworkUrl.toUri())
            .build()
        val builder = MediaItem.Builder()
            .setUri(playbackDecision.url.toUri())
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .setTag(playbackReportContext)

        playbackCustomCacheKey(
            mediaId = mediaId,
            playbackUrl = playbackDecision.url,
            playMethod = playbackDecision.reportContext.playMethod,
        )?.let(builder::setCustomCacheKey)

        return builder.build()
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
