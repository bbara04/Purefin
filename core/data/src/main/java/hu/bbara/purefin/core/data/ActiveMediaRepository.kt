package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.data.local.room.OfflineRepository
import hu.bbara.purefin.core.data.local.room.OnlineRepository
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Library
import hu.bbara.purefin.core.model.Media
import hu.bbara.purefin.core.model.MediaRepositoryState
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Active media repository that delegates to either online or offline repository
 * based on user preference.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ActiveMediaRepository @Inject constructor(
    @OnlineRepository private val onlineRepository: MediaRepository,
    @OfflineRepository private val offlineRepository: MediaRepository,
    private val userSessionRepository: UserSessionRepository
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Switch between repositories based on offline mode preference
    private val activeRepository: StateFlow<MediaRepository> =
        userSessionRepository.isOfflineMode
            .map { isOffline ->
                if (isOffline) offlineRepository else onlineRepository
            }
            .stateIn(scope, SharingStarted.Eagerly, onlineRepository)

    // Delegate all MediaRepository interface methods to the active repository
    override val libraries: StateFlow<List<Library>> =
        activeRepository.flatMapLatest { it.libraries }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val movies: StateFlow<Map<UUID, Movie>> =
        activeRepository.flatMapLatest { it.movies }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> =
        activeRepository.flatMapLatest { it.series }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> =
        activeRepository.flatMapLatest { it.episodes }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val state: StateFlow<MediaRepositoryState> =
        activeRepository.flatMapLatest { it.state }
            .stateIn(scope, SharingStarted.Eagerly, MediaRepositoryState.Loading)

    override val continueWatching: StateFlow<List<Media>> =
        activeRepository.flatMapLatest { it.continueWatching }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val nextUp: StateFlow<List<Media>> =
        activeRepository.flatMapLatest { it.nextUp }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val latestLibraryContent: StateFlow<Map<UUID, List<Media>>> =
        activeRepository.flatMapLatest { it.latestLibraryContent }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> =
        activeRepository.flatMapLatest { it.observeSeriesWithContent(seriesId) }

    override suspend fun ensureReady() {
        activeRepository.value.ensureReady()
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        activeRepository.value.updateWatchProgress(mediaId, positionMs, durationMs)
    }

    override suspend fun refreshHomeData() {
        activeRepository.value.refreshHomeData()
    }
}
