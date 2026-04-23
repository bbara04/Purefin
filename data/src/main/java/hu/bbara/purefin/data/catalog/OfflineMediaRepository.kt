package hu.bbara.purefin.data.catalog

import hu.bbara.purefin.core.data.MediaCatalogReader
import hu.bbara.purefin.core.data.MediaProgressWriter
import hu.bbara.purefin.core.data.OfflineCatalogReader
import hu.bbara.purefin.data.offline.room.offline.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Series
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@Singleton
class OfflineMediaRepository @Inject constructor(
    private val localDataSource: OfflineRoomMediaLocalDataSource,
) : OfflineCatalogReader, MediaCatalogReader, MediaProgressWriter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val movies: StateFlow<Map<UUID, Movie>> = localDataSource.moviesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val series: StateFlow<Map<UUID, Series>> = localDataSource.seriesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val episodes: StateFlow<Map<UUID, Episode>> = localDataSource.episodesFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun observeSeriesWithContent(seriesId: UUID): Flow<Series?> {
        return localDataSource.observeSeriesWithContent(seriesId)
    }

    override suspend fun updateWatchProgress(mediaId: UUID, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val progressPercent = (positionMs.toDouble() / durationMs.toDouble()) * 100.0
        val watched = progressPercent >= 90.0
        localDataSource.updateWatchProgress(mediaId, progressPercent, watched)
    }
}
