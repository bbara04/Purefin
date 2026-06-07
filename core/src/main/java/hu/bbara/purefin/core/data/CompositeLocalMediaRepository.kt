package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.Online
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import hu.bbara.purefin.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CompositeLocalMediaRepository @Inject constructor(
    @Offline private val offlineRepository: LocalMediaRepository,
    @Online private val onlineRepository: LocalMediaRepository,
    private val networkMonitor: NetworkMonitor,
) : LocalMediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activeRepository: Flow<LocalMediaRepository> = networkMonitor.isOnline
        .map { isOnline -> if (isOnline) onlineRepository else offlineRepository }

    override val movies: StateFlow<Map<UUID, Movie>> = activeRepository
        .flatMapLatest { it.movies }
        .stateIn(scope, Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = activeRepository
        .flatMapLatest { it.series }
        .stateIn(scope, Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = activeRepository
        .flatMapLatest { it.episodes }
        .stateIn(scope, Eagerly, emptyMap())

    override suspend fun getMovie(id: UUID): Flow<Movie?> {
        return getFromActiveRepository(
            onlineRead = { onlineRepository.getMovie(id) },
            offlineRead = { offlineRepository.getMovie(id) },
        )
    }

    override suspend fun getSeries(id: UUID): Flow<Series?> {
        return getFromActiveRepository(
            onlineRead = { onlineRepository.getSeries(id) },
            offlineRead = { offlineRepository.getSeries(id) },
        )
    }

    override suspend fun getEpisode(id: UUID): Flow<Episode?> {
        return getFromActiveRepository(
            onlineRead = { onlineRepository.getEpisode(id) },
            offlineRead = { offlineRepository.getEpisode(id) },
        )
    }

    override suspend fun loadSeasons(seriesId: UUID) {
        runOnlineOrOfflineNoOp(
            onlineAction = { onlineRepository.loadSeasons(seriesId) },
            offlineAction = { offlineRepository.loadSeasons(seriesId) },
        )
    }

    override suspend fun loadSeasonEpisodes(seriesId: UUID, seasonId: UUID) {
        runOnlineOrOfflineNoOp(
            onlineAction = { onlineRepository.loadSeasonEpisodes(seriesId, seasonId) },
            offlineAction = { offlineRepository.loadSeasonEpisodes(seriesId, seasonId) },
        )
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (networkMonitor.isOnline.first()) {
            runOnlineAction { onlineRepository.updateWatchProgress(mediaId, positionMs, durationMs) }
        }
        offlineRepository.updateWatchProgress(mediaId, positionMs, durationMs)
    }

    override suspend fun updateWatchProgressPercent(mediaId: UUID, progressPercent: Double) {
        if (networkMonitor.isOnline.first()) {
            runOnlineAction { onlineRepository.updateWatchProgressPercent(mediaId, progressPercent) }
        }
        offlineRepository.updateWatchProgressPercent(mediaId, progressPercent)
    }

    override suspend fun markAsWatched(mediaId: UUID, watched: Boolean) {
        runOnlineOrOfflineNoOp(
            onlineAction = { onlineRepository.markAsWatched(mediaId, watched) },
            offlineAction = { offlineRepository.markAsWatched(mediaId, watched) },
        )
    }

    private suspend fun <T> getFromActiveRepository(
        onlineRead: suspend () -> Flow<T>,
        offlineRead: suspend () -> Flow<T>,
    ): Flow<T> {
        if (!networkMonitor.isOnline.first()) {
            return offlineRead()
        }
        return try {
            onlineRead()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (!networkMonitor.checkConnection()) {
                offlineRead()
            } else {
                throw error
            }
        }
    }

    private suspend fun runOnlineOrOfflineNoOp(
        onlineAction: suspend () -> Unit,
        offlineAction: suspend () -> Unit,
    ) {
        if (!networkMonitor.isOnline.first()) {
            offlineAction()
            return
        }
        try {
            onlineAction()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (!networkMonitor.checkConnection()) {
                offlineAction()
            } else {
                throw error
            }
        }
    }

    private suspend fun runOnlineAction(action: suspend () -> Unit) {
        try {
            action()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (networkMonitor.checkConnection()) {
                throw error
            }
        }
    }
}
