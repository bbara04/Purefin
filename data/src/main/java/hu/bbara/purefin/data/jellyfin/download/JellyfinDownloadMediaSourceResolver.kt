package hu.bbara.purefin.data.jellyfin.download

import hu.bbara.purefin.core.data.DownloadMediaSourceResolver
import hu.bbara.purefin.core.data.EpisodeDownloadSource
import hu.bbara.purefin.core.data.MovieDownloadSource
import hu.bbara.purefin.core.data.PlaybackMethod
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.JellyfinPlaybackResolver
import hu.bbara.purefin.data.jellyfin.playback.PlaybackDecision
import hu.bbara.purefin.data.jellyfin.playback.playbackCustomCacheKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JellyfinDownloadMediaSourceResolver @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinPlaybackResolver: JellyfinPlaybackResolver,
) : DownloadMediaSourceResolver {
    override suspend fun resolveMovieDownload(movieId: UUID): MovieDownloadSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val playbackDecision = jellyfinPlaybackResolver.getPlaybackDecision(movieId) ?: return@withContext null
        val itemInfo = jellyfinApiClient.getItemInfo(movieId) ?: return@withContext null

        MovieDownloadSource(
            movie = itemInfo.toMovie(serverUrl),
            playbackUrl = playbackDecision.url,
            customCacheKey = playbackDecision.downloadCustomCacheKey(movieId),
        )
    }

    override suspend fun resolveEpisodeDownload(episodeId: UUID): EpisodeDownloadSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val playbackDecision = jellyfinPlaybackResolver.getPlaybackDecision(episodeId) ?: return@withContext null
        val episodeDto = jellyfinApiClient.getItemInfo(episodeId) ?: return@withContext null
        val episode = episodeDto.toEpisode(serverUrl)
        val series = jellyfinApiClient.getItemInfo(episode.seriesId)?.toSeries(serverUrl) ?: return@withContext null
        val season = jellyfinApiClient.getItemInfo(episode.seasonId)?.toSeason() ?: return@withContext null

        EpisodeDownloadSource(
            episode = episode,
            series = series,
            season = season,
            playbackUrl = playbackDecision.url,
            customCacheKey = playbackDecision.downloadCustomCacheKey(episodeId),
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

    private fun PlaybackDecision.downloadCustomCacheKey(mediaId: UUID): String? {
        if (reportContext.playMethod != PlaybackMethod.DIRECT_PLAY) {
            return null
        }
        return playbackCustomCacheKey(
            mediaId = mediaId.toString(),
            playbackUrl = url,
            playMethod = reportContext.playMethod,
        )
    }
}
