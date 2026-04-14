package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
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
