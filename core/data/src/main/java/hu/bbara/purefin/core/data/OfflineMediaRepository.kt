package hu.bbara.purefin.core.data

import hu.bbara.purefin.core.data.room.offline.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val localDataSource: OfflineRoomMediaLocalDataSource
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val movies: StateFlow<Map<UUID, Movie>> = localDataSource.moviesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val series: StateFlow<Map<UUID, Series>> = localDataSource.seriesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val episodes: StateFlow<Map<UUID, Episode>> = localDataSource.episodesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return localDataSource.observeSeriesWithContent(seriesId)
    }

    suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0
        // Write to offline database - the reactive Flows propagate changes to UI automatically
        localDataSource.updateWatchProgress(mediaId, progressPercent, watched)
    }

}
