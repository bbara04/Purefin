package hu.bbara.purefin.core.download

import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface MediaDownloadController {
    fun observeActiveDownloads(): Flow<Map<String, Float>>
    fun observeDownloadState(contentId: String): StateFlow<DownloadState>
    fun getCompletedDownloadMediaItem(contentId: String): MediaItem?
    suspend fun downloadMovie(movieId: UUID)
    suspend fun cancelDownload(movieId: UUID)
    suspend fun downloadEpisode(episodeId: UUID)
    suspend fun downloadEpisodes(episodeIds: List<UUID>)
    suspend fun cancelEpisodeDownload(episodeId: UUID)
    suspend fun enableSmartDownload(seriesId: UUID)
    suspend fun deleteSmartDownloads(seriesId: UUID)
    fun isSmartDownloadEnabled(seriesId: UUID): Flow<Boolean>
    suspend fun syncSmartDownloads()
}
