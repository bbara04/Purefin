package hu.bbara.purefin.data.jellyfin.download

import hu.bbara.purefin.data.DownloadMediaSourceResolver
import hu.bbara.purefin.data.EpisodeDownloadSource
import hu.bbara.purefin.data.MovieDownloadSource
import hu.bbara.purefin.data.PlaybackMethod
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.PlaybackDecisionResolver
import hu.bbara.purefin.data.jellyfin.playback.playbackCustomCacheKey
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.MediaSourceInfo

@Singleton
class JellyfinDownloadMediaSourceResolver @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
) : DownloadMediaSourceResolver {
    override suspend fun resolveMovieDownload(movieId: UUID): MovieDownloadSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val mediaSource = jellyfinApiClient.getMediaSources(movieId).firstOrNull() ?: return@withContext null
        val playbackUrl = resolvePlaybackUrl(movieId, mediaSource, serverUrl) ?: return@withContext null
        val itemInfo = jellyfinApiClient.getItemInfo(movieId) ?: return@withContext null

        MovieDownloadSource(
            movie = itemInfo.toMovie(serverUrl),
            playbackUrl = playbackUrl,
            customCacheKey = mediaSource.downloadCustomCacheKey(movieId, playbackUrl),
        )
    }

    override suspend fun resolveEpisodeDownload(episodeId: UUID): EpisodeDownloadSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val mediaSource = jellyfinApiClient.getMediaSources(episodeId).firstOrNull() ?: return@withContext null
        val playbackUrl = resolvePlaybackUrl(episodeId, mediaSource, serverUrl) ?: return@withContext null
        val episodeDto = jellyfinApiClient.getItemInfo(episodeId) ?: return@withContext null
        val episode = episodeDto.toEpisode(serverUrl)
        val series = jellyfinApiClient.getItemInfo(episode.seriesId)?.toSeries(serverUrl) ?: return@withContext null
        val season = jellyfinApiClient.getItemInfo(episode.seasonId)?.toSeason(series.id) ?: return@withContext null

        EpisodeDownloadSource(
            episode = episode,
            series = series,
            season = season,
            playbackUrl = playbackUrl,
            customCacheKey = mediaSource.downloadCustomCacheKey(episodeId, playbackUrl),
        )
    }

    override suspend fun isEpisodeWatched(episodeId: UUID): Boolean {
        return jellyfinApiClient.getItemInfo(episodeId)?.userData?.played == true
    }

    override suspend fun getUnwatchedEpisodeIds(
        seriesId: UUID,
        excludedEpisodeIds: Set<UUID>,
        limit: Int,
    ): List<UUID> = withContext(Dispatchers.IO) {
        if (limit <= 0) {
            return@withContext emptyList()
        }

        val seasons = jellyfinApiClient.getSeasons(seriesId)
        val episodes = buildList {
            seasons.forEach { season ->
                addAll(jellyfinApiClient.getEpisodesInSeason(seriesId, season.id))
            }
        }

        episodes
            .filter { episode ->
                episode.userData?.played != true && episode.id !in excludedEpisodeIds
            }
            .take(limit)
            .map { it.id }
    }

    private fun resolvePlaybackUrl(
        mediaId: UUID,
        mediaSource: MediaSourceInfo,
        serverUrl: String,
    ): String? {
        val shouldTranscode = mediaSource.supportsTranscoding == true &&
            (mediaSource.supportsDirectPlay == false || mediaSource.transcodingUrl != null)

        return if (shouldTranscode && !mediaSource.transcodingUrl.isNullOrBlank()) {
            PlaybackDecisionResolver.absolutePlaybackUrl(serverUrl, requireNotNull(mediaSource.transcodingUrl))
        } else {
            jellyfinApiClient.getVideoStreamUrl(
                itemId = mediaId,
                mediaSourceId = mediaSource.id,
            )
        }
    }

    private fun MediaSourceInfo.downloadCustomCacheKey(
        mediaId: UUID,
        playbackUrl: String,
    ): String? {
        val shouldTranscode = supportsTranscoding == true &&
            (supportsDirectPlay == false || transcodingUrl != null)
        if (shouldTranscode) {
            return null
        }
        return playbackCustomCacheKey(
            mediaId = mediaId.toString(),
            playbackUrl = playbackUrl,
            playMethod = PlaybackMethod.DIRECT_PLAY,
        )
    }
}
