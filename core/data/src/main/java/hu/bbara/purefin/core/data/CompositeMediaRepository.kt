package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CompositeMediaRepository @Inject constructor(
    private val offlineRepository: OfflineMediaRepository,
    private val onlineRepository: InMemoryMediaRepository,
    private val networkMonitor: NetworkMonitor,
) : MediaCatalogReader, MediaProgressWriter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activeRepository: Flow<MediaCatalogReader> =
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

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return activeRepository.flatMapLatest { it.observeSeriesWithContent(seriesId) }
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        val repository = if (networkMonitor.isOnline.first()) onlineRepository else offlineRepository
        repository.updateWatchProgress(mediaId, positionMs, durationMs)
    }
}
