package hu.bbara.purefin.core.data

import hu.bbara.purefin.model.DownloadedSubtitle
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import java.util.UUID

interface OfflineMediaManager {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveSeries(series: List<Series>)
    suspend fun saveSeason(season: Season)
    suspend fun saveEpisode(episode: Episode)
    suspend fun getSeriesBasic(seriesId: UUID): Series?
    suspend fun getSeason(seasonId: UUID): Season?
    suspend fun deleteMovie(movieId: UUID)
    suspend fun deleteEpisodeAndCleanup(episodeId: UUID)
    suspend fun getEpisodesBySeries(seriesId: UUID): List<Episode>
    suspend fun saveSubtitles(mediaId: UUID, subtitles: List<DownloadedSubtitle>)
    suspend fun getSubtitles(mediaId: UUID): List<DownloadedSubtitle>
    suspend fun deleteSubtitles(mediaId: UUID)
}
