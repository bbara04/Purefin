package hu.bbara.purefin.data

import hu.bbara.purefin.data.local.room.OfflineDatabase
import hu.bbara.purefin.data.local.room.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Library
import hu.bbara.purefin.data.model.Media
import hu.bbara.purefin.data.model.Movie
import hu.bbara.purefin.data.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline media repository for managing downloaded content.
 * This repository only accesses the local offline database and does not make network calls.
 */
@Singleton
class OfflineMediaRepository @Inject constructor(
    @OfflineDatabase private val localDataSource: OfflineRoomMediaLocalDataSource
) : MediaRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Offline repository is always ready (no network loading required)
    private val _state: MutableStateFlow<MediaRepositoryState> = MutableStateFlow(MediaRepositoryState.Ready)
    override val state: StateFlow<MediaRepositoryState> = _state.asStateFlow()

    override val libraries: StateFlow<List<Library>> = localDataSource.librariesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val movies: StateFlow<Map<UUID, Movie>> = localDataSource.moviesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = localDataSource.seriesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = localDataSource.episodesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    // Offline mode doesn't support these server-side features
    override val continueWatching: StateFlow<List<Media>> = MutableStateFlow(emptyList())
    override val nextUp: StateFlow<List<Media>> = MutableStateFlow(emptyList())
    override val latestLibraryContent: StateFlow<Map<UUID, List<Media>>> = MutableStateFlow(emptyMap())

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return localDataSource.observeSeriesWithContent(seriesId)
    }

    override suspend fun ensureReady() {
        // Offline repository is always ready - no initialization needed
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0
        // Write to offline database - the reactive Flows propagate changes to UI automatically
        localDataSource.updateWatchProgress(mediaId, progressPercent, watched)
    }

    override suspend fun refreshHomeData() {
        // No-op for offline repository - no network refresh available
    }
}
