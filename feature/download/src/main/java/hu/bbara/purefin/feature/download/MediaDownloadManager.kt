package hu.bbara.purefin.feature.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.local.room.OfflineDatabase
import hu.bbara.purefin.core.data.local.room.OfflineRoomMediaLocalDataSource
import hu.bbara.purefin.core.data.local.room.dao.MovieDao
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.ImageType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class MediaDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager,
    private val jellyfinApiClient: JellyfinApiClient,
    @OfflineDatabase private val offlineDataSource: OfflineRoomMediaLocalDataSource,
    @OfflineDatabase private val movieDao: MovieDao,
    private val userSessionRepository: UserSessionRepository
) {

    private val stateFlows = ConcurrentHashMap<String, MutableStateFlow<DownloadState>>()

    init {
        downloadManager.resumeDownloads()

        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                manager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                val contentId = download.request.id
                val state = download.toDownloadState()
                Log.d(TAG, "Download changed: $contentId -> $state (${download.percentDownloaded}%)")
                if (finalException != null) {
                    Log.e(TAG, "Download exception for $contentId", finalException)
                }
                getOrCreateStateFlow(contentId).value = state
            }

            override fun onDownloadRemoved(manager: DownloadManager, download: Download) {
                val contentId = download.request.id
                Log.d(TAG, "Download removed: $contentId")
                getOrCreateStateFlow(contentId).value = DownloadState.NotDownloaded
            }
        })
    }

    fun observeDownloadState(contentId: String): StateFlow<DownloadState> {
        val flow = getOrCreateStateFlow(contentId)
        // Initialize from current download index
        val download = downloadManager.downloadIndex.getDownload(contentId)
        flow.value = download?.toDownloadState() ?: DownloadState.NotDownloaded
        return flow
    }

    fun isDownloaded(contentId: String): Boolean {
        return downloadManager.downloadIndex.getDownload(contentId)?.state == Download.STATE_COMPLETED
    }

    suspend fun downloadMovie(movieId: UUID) {
        withContext(Dispatchers.IO) {
            try {
                val sources = jellyfinApiClient.getMediaSources(movieId)
                val source = sources.firstOrNull() ?: run {
                    Log.e(TAG, "No media sources for $movieId")
                    return@withContext
                }

                val url = jellyfinApiClient.getMediaPlaybackUrl(movieId, source) ?: run {
                    Log.e(TAG, "No playback URL for $movieId")
                    return@withContext
                }

                val itemInfo = jellyfinApiClient.getItemInfo(movieId) ?: run {
                    Log.e(TAG, "No item info for $movieId")
                    return@withContext
                }

                val serverUrl = userSessionRepository.serverUrl.first().trim()
                val movie = Movie(
                    id = itemInfo.id,
                    libraryId = itemInfo.parentId ?: UUID.randomUUID(),
                    title = itemInfo.name ?: "Unknown title",
                    progress = itemInfo.userData?.playedPercentage,
                    watched = itemInfo.userData?.played ?: false,
                    year = itemInfo.productionYear?.toString()
                        ?: itemInfo.premiereDate?.year?.toString().orEmpty(),
                    rating = itemInfo.officialRating ?: "NR",
                    runtime = formatRuntime(itemInfo.runTimeTicks),
                    synopsis = itemInfo.overview ?: "No synopsis available",
                    format = itemInfo.container?.uppercase() ?: "VIDEO",
                    heroImageUrl = JellyfinImageHelper.toImageUrl(
                        url = serverUrl,
                        itemId = itemInfo.id,
                        type = ImageType.PRIMARY
                    ),
                    subtitles = "ENG",
                    audioTrack = "ENG",
                    cast = emptyList()
                )

                offlineDataSource.saveMovies(listOf(movie))

                Log.d(TAG, "Starting download for '${movie.title}' from: $url")
                val request = DownloadRequest.Builder(movieId.toString(), url.toUri()).build()
                PurefinDownloadService.sendAddDownload(context, request)
                Log.d(TAG, "Download request sent for $movieId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start download for $movieId", e)
                getOrCreateStateFlow(movieId.toString()).value = DownloadState.Failed
            }
        }
    }

    suspend fun cancelDownload(movieId: UUID) {
        withContext(Dispatchers.IO) {
            PurefinDownloadService.sendRemoveDownload(context, movieId.toString())
            try {
                movieDao.deleteById(movieId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove movie from offline DB", e)
            }
        }
    }

    private fun getOrCreateStateFlow(contentId: String): MutableStateFlow<DownloadState> {
        return stateFlows.getOrPut(contentId) { MutableStateFlow(DownloadState.NotDownloaded) }
    }

    private fun Download.toDownloadState(): DownloadState = when (state) {
        Download.STATE_COMPLETED -> DownloadState.Downloaded
        Download.STATE_DOWNLOADING -> DownloadState.Downloading(percentDownloaded)
        Download.STATE_QUEUED, Download.STATE_RESTARTING -> DownloadState.Downloading(0f)
        Download.STATE_FAILED -> DownloadState.Failed
        Download.STATE_REMOVING -> DownloadState.NotDownloaded
        Download.STATE_STOPPED -> DownloadState.NotDownloaded
        else -> DownloadState.NotDownloaded
    }

    private fun formatRuntime(ticks: Long?): String {
        if (ticks == null || ticks <= 0) return "—"
        val totalSeconds = ticks / 10_000_000
        val hours = java.util.concurrent.TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = java.util.concurrent.TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    companion object {
        private const val TAG = "MediaDownloadManager"
    }
}
