package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.OfflineMediaManager
import hu.bbara.purefin.data.offline.room.offline.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineLocalMediaRepository @Inject constructor(
    private val localDataSource: OfflineRoomMediaLocalDataSource,
) : LocalMediaRepository, OfflineMediaManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val movies: StateFlow<Map<UUID, Movie>> = localDataSource.moviesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = localDataSource.seriesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = localDataSource.episodesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override suspend fun getMovie(id: UUID): Flow<Movie?> {
        return movies.map { it[id] }
    }

    override suspend fun getSeries(id: UUID): Flow<Series?> {
        return localDataSource.observeSeriesWithContent(id)
    }

    override suspend fun getEpisode(id: UUID): Flow<Episode?> {
        return episodes.map { it[id] }
    }

    override suspend fun loadSeasons(seriesId: UUID) {
        // Offline series content is already emitted with its saved seasons.
    }

    override suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID) {
        // Offline series content is already emitted with its saved episodes.
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        updateWatchProgressPercent(mediaId, progressPercent)
    }

    override suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double) {
        if (progressPercent.isNaN()) return
        val normalizedProgressPercent = progressPercent.coerceIn(0.0, 100.0)
        val watched = normalizedProgressPercent >= 90.0
        localDataSource.updateWatchProgress(mediaId, normalizedProgressPercent, watched)
    }

    override suspend fun markAsWatched(mediaId: UUID, watched: Boolean) {
        // Do nothing
    }

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
