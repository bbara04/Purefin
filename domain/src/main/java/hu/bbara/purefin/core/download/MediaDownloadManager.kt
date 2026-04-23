package hu.bbara.purefin.core.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.DownloadMediaSourceResolver
import hu.bbara.purefin.core.data.OfflineCatalogStore
import hu.bbara.purefin.core.data.SmartDownloadStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class MediaDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager,
    private val downloadMediaSourceResolver: DownloadMediaSourceResolver,
    private val offlineCatalogStore: OfflineCatalogStore,
    private val smartDownloadStore: SmartDownloadStore,
) : MediaDownloadController {

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

    /**
     * Polls the download index every 500 ms and emits a map of contentId → progress (0–100)
     * for every download that is currently queued or in progress.
     * Uses the download index directly so it always reflects the true download state,
     * regardless of listener callback timing.
     */
    override fun observeActiveDownloads(): Flow<Map<String, Float>> = flow {
        while (true) {
            try {
                val result = buildMap<String, Float> {
                    val cursor = downloadManager.downloadIndex.getDownloads(
                        Download.STATE_QUEUED,
                        Download.STATE_DOWNLOADING,
                        Download.STATE_RESTARTING
                    )
                    cursor.use {
                        while (it.moveToNext()) {
                            val d = it.download
                            put(d.request.id, d.percentDownloaded)
                        }
                    }
                }
                emit(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to poll active downloads", e)
                emit(emptyMap())
            }
            delay(500)
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    override fun observeDownloadState(contentId: String): StateFlow<DownloadState> {
        val flow = getOrCreateStateFlow(contentId)
        // Initialize from current download index
        val download = downloadManager.downloadIndex.getDownload(contentId)
        flow.value = download?.toDownloadState() ?: DownloadState.NotDownloaded
        return flow
    }

    fun isDownloaded(contentId: String): Boolean {
        return downloadManager.downloadIndex.getDownload(contentId)?.state == Download.STATE_COMPLETED
    }

    override suspend fun downloadMovie(movieId: UUID) {
        withContext(Dispatchers.IO) {
            try {
                val source = downloadMediaSourceResolver.resolveMovieDownload(movieId) ?: run {
                    Log.e(TAG, "No downloadable movie source for $movieId")
                    return@withContext
                }

                offlineCatalogStore.saveMovies(listOf(source.movie))

                Log.d(TAG, "Starting download for '${source.movie.title}' from: ${source.playbackUrl}")
                val request = buildDownloadRequest(
                    mediaId = movieId,
                    playbackUrl = source.playbackUrl,
                    title = source.movie.title,
                    customCacheKey = source.customCacheKey,
                )
                PurefinDownloadService.sendAddDownload(context, request)
                Log.d(TAG, "Download request sent for $movieId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start download for $movieId", e)
                getOrCreateStateFlow(movieId.toString()).value = DownloadState.Failed
            }
        }
    }

    override suspend fun cancelDownload(movieId: UUID) {
        withContext(Dispatchers.IO) {
            PurefinDownloadService.sendRemoveDownload(context, movieId.toString())
            try {
                offlineCatalogStore.deleteMovie(movieId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove movie from offline DB", e)
            }
        }
    }

    override suspend fun downloadEpisode(episodeId: UUID) {
        withContext(Dispatchers.IO) {
            try {
                val source = downloadMediaSourceResolver.resolveEpisodeDownload(episodeId) ?: run {
                    Log.e(TAG, "No downloadable episode source for $episodeId")
                    return@withContext
                }

                if (offlineCatalogStore.getSeriesBasic(source.series.id) == null) {
                    offlineCatalogStore.saveSeries(listOf(source.series))
                }

                if (offlineCatalogStore.getSeason(source.season.id) == null) {
                    offlineCatalogStore.saveSeason(source.season)
                }

                offlineCatalogStore.saveEpisode(source.episode)

                Log.d(TAG, "Starting download for episode '${source.episode.title}' from: ${source.playbackUrl}")
                val request = buildDownloadRequest(
                    mediaId = episodeId,
                    playbackUrl = source.playbackUrl,
                    title = source.episode.title,
                    customCacheKey = source.customCacheKey,
                )
                PurefinDownloadService.sendAddDownload(context, request)
                Log.d(TAG, "Download request sent for episode $episodeId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start download for episode $episodeId", e)
                getOrCreateStateFlow(episodeId.toString()).value = DownloadState.Failed
            }
        }
    }

    override suspend fun downloadEpisodes(episodeIds: List<UUID>) {
        coroutineScope {
            for (episodeId in episodeIds) {
                launch { downloadEpisode(episodeId) }
            }
        }
    }

    override suspend fun cancelEpisodeDownload(episodeId: UUID) {
        withContext(Dispatchers.IO) {
            PurefinDownloadService.sendRemoveDownload(context, episodeId.toString())
            try {
                offlineCatalogStore.deleteEpisodeAndCleanup(episodeId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove episode from offline DB", e)
            }
        }
    }

    // ── Smart Download ──────────────────────────────────────────────────

    override suspend fun enableSmartDownload(seriesId: UUID) {
        smartDownloadStore.enable(seriesId)
        syncSmartDownloadsForSeries(seriesId)
    }

    suspend fun disableSmartDownload(seriesId: UUID) {
        smartDownloadStore.disable(seriesId)
    }

    fun isSmartDownloadEnabled(seriesId: UUID): Flow<Boolean> = smartDownloadStore.observe(seriesId)

    override suspend fun syncSmartDownloads() {
        withContext(Dispatchers.IO) {
            val enabledSeriesIds = smartDownloadStore.getEnabledSeriesIds()
            for (seriesId in enabledSeriesIds) {
                try {
                    syncSmartDownloadsForSeries(seriesId)
                } catch (e: Exception) {
                    Log.e(TAG, "Smart download sync failed for series $seriesId", e)
                }
            }
        }
    }

    private suspend fun syncSmartDownloadsForSeries(seriesId: UUID) {
        withContext(Dispatchers.IO) {
            // 1. Get currently downloaded episodes for this series
            val downloadedEpisodes = offlineCatalogStore.getEpisodesBySeries(seriesId)

            // 2. Check watched status from server and delete watched downloads
            val unwatchedDownloaded = mutableListOf<UUID>()
            for (episode in downloadedEpisodes) {
                if (downloadMediaSourceResolver.isEpisodeWatched(episode.id)) {
                    Log.d(TAG, "Smart download: removing watched episode ${episode.title}")
                    cancelEpisodeDownload(episode.id)
                } else {
                    unwatchedDownloaded.add(episode.id)
                }
            }

            // 3. Find unwatched episodes not already downloaded
            val needed = SMART_DOWNLOAD_COUNT - unwatchedDownloaded.size
            if (needed <= 0) return@withContext

            val toDownload = downloadMediaSourceResolver.getUnwatchedEpisodeIds(
                seriesId = seriesId,
                excludedEpisodeIds = unwatchedDownloaded.toSet(),
                limit = needed,
            )

            if (toDownload.isNotEmpty()) {
                Log.d(TAG, "Smart download: queuing ${toDownload.size} episodes for series $seriesId")
                downloadEpisodes(toDownload)
            }
        }
    }

    private fun getOrCreateStateFlow(contentId: String): MutableStateFlow<DownloadState> {
        return stateFlows.getOrPut(contentId) { MutableStateFlow(DownloadState.NotDownloaded) }
    }

    private fun buildDownloadRequest(
        mediaId: UUID,
        playbackUrl: String,
        title: String,
        customCacheKey: String?,
    ): DownloadRequest {
        val builder = DownloadRequest.Builder(mediaId.toString(), playbackUrl.toUri())
            .setData(title.toByteArray(Charsets.UTF_8))

        customCacheKey?.let(builder::setCustomCacheKey)

        return builder.build()
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

    companion object {
        private const val TAG = "MediaDownloadManager"
        private const val SMART_DOWNLOAD_COUNT = 5
    }
}
