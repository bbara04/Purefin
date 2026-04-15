package hu.bbara.purefin.data.offline

import hu.bbara.purefin.core.data.OfflineCatalogStore
import hu.bbara.purefin.data.offline.room.offline.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomOfflineCatalogStore @Inject constructor(
    private val localDataSource: OfflineRoomMediaLocalDataSource,
) : OfflineCatalogStore {
    override suspend fun saveMovies(movies: List<Movie>) {
        localDataSource.saveMovies(movies)
    }

    override suspend fun saveSeries(series: List<Series>) {
        localDataSource.saveSeries(series)
    }

    override suspend fun saveSeason(season: Season) {
        localDataSource.saveSeason(season)
    }

    override suspend fun saveEpisode(episode: Episode) {
        localDataSource.saveEpisode(episode)
    }

    override suspend fun getSeriesBasic(seriesId: UUID): Series? {
        return localDataSource.getSeriesBasic(seriesId)
    }

    override suspend fun getSeason(seasonId: UUID): Season? {
        return localDataSource.getSeason(seasonId)
    }

    override suspend fun deleteMovie(movieId: UUID) {
        localDataSource.deleteMovie(movieId)
    }

    override suspend fun deleteEpisodeAndCleanup(episodeId: UUID) {
        localDataSource.deleteEpisodeAndCleanup(episodeId)
    }

    override suspend fun getEpisodesBySeries(seriesId: UUID): List<Episode> {
        return localDataSource.getEpisodesBySeries(seriesId)
    }
}
