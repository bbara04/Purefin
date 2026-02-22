package hu.bbara.purefin.core.data

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
import kotlinx.coroutines.flow.flatMapLatest
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
    private val userSessionRepository: UserSessionRepository,
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activeRepository: Flow<MediaRepository> =
        userSessionRepository.isOfflineMode.flatMapLatest { offline ->
            kotlinx.coroutines.flow.flowOf(if (offline) offlineRepository else onlineRepository)
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
        val isOffline = userSessionRepository.isOfflineMode.stateIn(scope).value
        val repo = if (isOffline) offlineRepository else onlineRepository
        repo.updateWatchProgress(mediaId, positionMs, durationMs)
    }
}
