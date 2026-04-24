package hu.bbara.purefin.data

import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import java.util.UUID

interface OfflineCatalogStore {
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveSeries(series: List<Series>)
    suspend fun saveSeason(season: Season)
    suspend fun saveEpisode(episode: Episode)
    suspend fun getSeriesBasic(seriesId: UUID): Series?
    suspend fun getSeason(seasonId: UUID): Season?
    suspend fun deleteMovie(movieId: UUID)
    suspend fun deleteEpisodeAndCleanup(episodeId: UUID)
    suspend fun getEpisodesBySeries(seriesId: UUID): List<Episode>
}
