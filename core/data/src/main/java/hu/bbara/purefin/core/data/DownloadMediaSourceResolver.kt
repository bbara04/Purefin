package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import java.util.UUID

interface DownloadMediaSourceResolver {
    suspend fun resolveMovieDownload(movieId: UUID): MovieDownloadSource?
    suspend fun resolveEpisodeDownload(episodeId: UUID): EpisodeDownloadSource?
    suspend fun isEpisodeWatched(episodeId: UUID): Boolean
    suspend fun getUnwatchedEpisodeIds(
        seriesId: UUID,
        excludedEpisodeIds: Set<UUID>,
        limit: Int,
    ): List<UUID>
}

data class MovieDownloadSource(
    val movie: Movie,
    val playbackUrl: String,
    val customCacheKey: String?,
)

data class EpisodeDownloadSource(
    val episode: Episode,
    val series: Series,
    val season: Season,
    val playbackUrl: String,
    val customCacheKey: String?,
)
