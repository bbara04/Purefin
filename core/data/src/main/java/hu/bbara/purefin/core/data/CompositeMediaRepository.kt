package hu.bbara.purefin.core.data

import android.util.Log
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Switches between [OfflineMediaRepository] and [AppContentRepository] based on
 * [UserSessionRepository.isOfflineMode]. When offline mode is enabled, all reads
 * and writes go through the offline (downloaded) repository; otherwise the online
 * repository is used.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CompositeMediaRepository @Inject constructor(
    private val offlineRepository: OfflineMediaRepository,
    private val onlineRepository: InMemoryMediaRepository,
    private val networkMonitor: NetworkMonitor,
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val ready: StateFlow<Boolean> = combine(
        offlineRepository.ready,
        onlineRepository.ready
    ) { offlineReady, onlineReady ->
        offlineReady && onlineReady
    }.stateIn(scope, SharingStarted.Eagerly, false)

    private val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val activeRepository: Flow<MediaRepository> =
        networkMonitor.isOnline.flatMapLatest { online ->
            flowOf(if (online) onlineRepository else offlineRepository)
        }

    override val movies: StateFlow<Map<UUID, Movie>> = activeRepository
        .flatMapLatest { it.movies }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = activeRepository
        .flatMapLatest { it.series }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = activeRepository
        .flatMapLatest { it.episodes }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun upsertMovies(movies: List<Movie>) {
        if (!isOnline.value) {
            Log.e("CompositeMediaRepository", "upsertMovies called in offline mode")
            return
        }
        onlineRepository.upsertMovies(movies)
    }

    override fun upsertSeries(series: List<Series>) {
        if (!isOnline.value) {
            Log.e("CompositeMediaRepository", "upsertSeries called in offline mode")
            return
        }
        onlineRepository.upsertSeries(series)
    }

    override fun upsertEpisodes(episodes: List<Episode>) {
        if (!isOnline.value) {
            Log.e("CompositeMediaRepository", "upsertEpisodes called in offline mode")
            return
        }
        onlineRepository.upsertEpisodes(episodes)
    }

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return activeRepository.flatMapLatest { it.observeSeriesWithContent(seriesId) }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        val isOnline = networkMonitor.isOnline.stateIn(scope).value
        val repo = if (isOnline) onlineRepository else offlineRepository
        repo.updateWatchProgress(mediaId, positionMs, durationMs)
    }

    override fun setReady() {
        onlineRepository.setReady()
        offlineRepository.setReady()
    }
}
