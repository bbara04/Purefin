package hu.bbara.purefin.data.jellyfin.download

import hu.bbara.purefin.core.data.DownloadMediaSourceResolver
import hu.bbara.purefin.core.data.EpisodeDownloadSource
import hu.bbara.purefin.core.data.MovieDownloadSource
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.converter.toEpisode
import hu.bbara.purefin.data.converter.toMovie
import hu.bbara.purefin.data.converter.toSeason
import hu.bbara.purefin.data.converter.toSeries
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.data.jellyfin.playback.JellyfinPlaybackResolver
import hu.bbara.purefin.data.jellyfin.playback.PlaybackSource
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

        val playbackSource = jellyfinPlaybackResolver.getPlaybackSource(movieId) ?: return@withContext null
        val itemInfo = jellyfinApiClient.getItemInfo(movieId) ?: return@withContext null

        MovieDownloadSource(
            movie = itemInfo.toMovie(serverUrl),
            playbackUrl = playbackSource.directPlayUrl,
            customCacheKey = playbackSource.downloadCustomCacheKey(movieId),
        )
    }

    override suspend fun resolveEpisodeDownload(episodeId: UUID): EpisodeDownloadSource? = withContext(Dispatchers.IO) {
        val serverUrl = userSessionRepository.serverUrl.first().trim()
        if (serverUrl.isBlank()) {
            return@withContext null
        }

        val playbackSource = jellyfinPlaybackResolver.getPlaybackSource(episodeId) ?: return@withContext null
        val episodeDto = jellyfinApiClient.getItemInfo(episodeId) ?: return@withContext null
        val episode = episodeDto.toEpisode(serverUrl)
        val series = jellyfinApiClient.getItemInfo(episode.seriesId)?.toSeries(serverUrl) ?: return@withContext null
        val season = jellyfinApiClient.getItemInfo(episode.seasonId)?.toSeason() ?: return@withContext null

        EpisodeDownloadSource(
            episode = episode,
            series = series,
            season = season,
            playbackUrl = playbackSource.directPlayUrl,
            customCacheKey = playbackSource.downloadCustomCacheKey(episodeId),
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

    private fun PlaybackSource.downloadCustomCacheKey(mediaId: UUID): String? {
        return playbackCustomCacheKey(
            mediaId = mediaId.toString(),
            playbackUrl = directPlayUrl,
            playMethod = playbackReportContext.playMethod,
        )
    }
}
