package hu.bbara.purefin.data.jellyfin

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import hu.bbara.purefin.data.PlayableMediaRepository
import hu.bbara.purefin.data.PlaybackReportContext
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.JellyfinPlaybackResolver
import hu.bbara.purefin.data.jellyfin.playback.PlaybackDecision
import hu.bbara.purefin.data.jellyfin.playback.playbackCustomCacheKey
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.MediaSegment
import hu.bbara.purefin.model.PlayableMedia
import hu.bbara.purefin.model.SegmentType
import hu.bbara.purefin.player.preference.TrackPreferencesRepository
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPlayableMediaRepository @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val jellyfinPlaybackResolver: JellyfinPlaybackResolver,
    private val trackPreferencesRepository: TrackPreferencesRepository,
    private val userSessionRepository: UserSessionRepository,
) : PlayableMediaRepository {

    override suspend fun getPlayableMedia(mediaId: UUID): PlayableMedia? = withContext(Dispatchers.IO) {
        val baseItem = jellyfinApiClient.getItemInfo(mediaId) ?: return@withContext null
        val playbackDecision = jellyfinPlaybackResolver.getPlaybackDecision(mediaId) ?: return@withContext null

        val mediaItem = getMediaItem(baseItem, playbackDecision)
        val resumePositionMs = calculateResumePosition(baseItem, playbackDecision.mediaSource)
        val mediaTrackPreferences = trackPreferencesRepository.getMediaPreferences(mediaId.toString()).first()
        val mediaSegments = getMediaSegments(mediaId)
        when (baseItem.type) {
            BaseItemKind.MOVIE -> PlayableMedia.Movie(
                id = mediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            BaseItemKind.SERIES -> PlayableMedia.Series(
                id = mediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            BaseItemKind.EPISODE -> PlayableMedia.Episode(
                id = mediaId,
                mediaItem = mediaItem,
                resumePositionMs = resumePositionMs ?: 0L,
                preferences = mediaTrackPreferences,
                mediaSegments = mediaSegments
            )
            else -> null
        }
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
            Log.w("PlayableMediaRepo", "Unable to load next-up items for $episodeId", error)
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
            startMs = startTicks,
            endMs = endTicks
        )
    }
}
