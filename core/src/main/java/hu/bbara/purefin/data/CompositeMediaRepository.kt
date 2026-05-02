package hu.bbara.purefin.data

import hu.bbara.purefin.Offline
import hu.bbara.purefin.Online
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Genre
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CompositeMediaRepository @Inject constructor(
    @Offline private val offlineRepository: MediaRepository,
    @Online private val onlineRepository: MediaRepository,
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // TODO move this into the domain layer and there you can use NetworkMonitor. Data should be free of android stuff.
    private val activeRepository: Flow<MediaRepository> = flowOf(onlineRepository)

    override val movies: StateFlow<Map<UUID, Movie>> = activeRepository
        .flatMapLatest { it.movies }
        .stateIn(scope, Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = activeRepository
        .flatMapLatest { it.series }
        .stateIn(scope, Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = activeRepository
        .flatMapLatest { it.episodes }
        .stateIn(scope, Eagerly, emptyMap())

    override val genres: StateFlow<Set<Genre>> = activeRepository
        .flatMapLatest { it.genres }
        .stateIn(scope, Eagerly, emptySet())

    override suspend fun getMovie(id: UUID): Flow<Movie?> {
        return activeRepository
            .flatMapLatest { it.getMovie(id) }
    }

    override suspend fun getSeries(id: UUID): Flow<Series?> {
        return activeRepository
            .flatMapLatest { it.getSeries(id) }
    }

    override suspend fun getEpisode(id: UUID): Flow<Episode?> {
        return activeRepository
            .flatMapLatest { it.getEpisode(id) }
    }

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return activeRepository.flatMapLatest { it.observeSeriesWithContent(seriesId) }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        val repository = onlineRepository
        repository.updateWatchProgress(mediaId, positionMs, durationMs)
    }
}
