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
import hu.bbara.purefin.core.data.OfflineCatalogStore
import hu.bbara.purefin.core.data.SmartDownloadStore
import hu.bbara.purefin.core.data.client.playbackCustomCacheKey
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.download.DownloadState
import hu.bbara.purefin.core.data.download.MediaDownloadController
import hu.bbara.purefin.core.data.session.UserSessionRepository
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Movie
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
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
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.PlayMethod
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class MediaDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager,
    private val jellyfinApiClient: JellyfinApiClient,
    private val offlineCatalogStore: OfflineCatalogStore,
    private val smartDownloadStore: SmartDownloadStore,
    private val userSessionRepository: UserSessionRepository,
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
                val sources = jellyfinApiClient.getMediaSources(movieId)
                val source = sources.firstOrNull() ?: run {
                    Log.e(TAG, "No media sources for $movieId")
                    return@withContext
                }
                val shouldTranscode = source.shouldUseTranscodingUrl()

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
                    imageUrlPrefix = JellyfinImageHelper.toPrefixImageUrl(
                        url = serverUrl,
                        itemId = itemInfo.id
                    ),
                    subtitles = "ENG",
                    audioTrack = "ENG",
                    cast = emptyList()
                )

                offlineCatalogStore.saveMovies(listOf(movie))

                Log.d(TAG, "Starting download for '${movie.title}' from: $url")
                val request = buildDownloadRequest(
                    mediaId = movieId,
                    playbackUrl = url,
                    title = movie.title,
                    isDirectPlay = !shouldTranscode
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
                val serverUrl = userSessionRepository.serverUrl.first().trim()
                val sources = jellyfinApiClient.getMediaSources(episodeId)
                val source = sources.firstOrNull() ?: run {
                    Log.e(TAG, "No media sources for episode $episodeId")
                    return@withContext
                }
                val shouldTranscode = source.shouldUseTranscodingUrl()

                val url = jellyfinApiClient.getMediaPlaybackUrl(episodeId, source) ?: run {
                    Log.e(TAG, "No playback URL for episode $episodeId")
                    return@withContext
                }

                val episode = jellyfinApiClient.getItemInfo(episodeId)?.toEpisode(serverUrl) ?: run {
                    Log.e(TAG, "Episode not found $episodeId")
                    return@withContext
                }

                val series = jellyfinApiClient.getItemInfo(episode.seriesId)?.toSeries(serverUrl) ?: run {
                    Log.e(TAG, "Series not found ${episode.seriesId}")
                    return@withContext
                }

                val season = jellyfinApiClient.getItemInfo(episode.seasonId)?.toSeason(series.id) ?: run {
                    Log.e(TAG, "Season not found ${episode.seasonId}")
                    return@withContext
                }

                if (offlineCatalogStore.getSeriesBasic(series.id) == null) {
                    offlineCatalogStore.saveSeries(listOf(series))
                }

                if (offlineCatalogStore.getSeason(season.id) == null) {
                    offlineCatalogStore.saveSeason(season)
                }

                offlineCatalogStore.saveEpisode(episode)

                Log.d(TAG, "Starting download for episode '${episode.title}' from: $url")
                val request = buildDownloadRequest(
                    mediaId = episodeId,
                    playbackUrl = url,
                    title = episode.title,
                    isDirectPlay = !shouldTranscode
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
            val serverUrl = userSessionRepository.serverUrl.first().trim()

            // 1. Get currently downloaded episodes for this series
            val downloadedEpisodes = offlineCatalogStore.getEpisodesBySeries(seriesId)

            // 2. Check watched status from server and delete watched downloads
            val unwatchedDownloaded = mutableListOf<UUID>()
            for (episode in downloadedEpisodes) {
                val itemInfo = jellyfinApiClient.getItemInfo(episode.id)
                val isWatched = itemInfo?.userData?.played ?: false
                if (isWatched) {
                    Log.d(TAG, "Smart download: removing watched episode ${episode.title}")
                    cancelEpisodeDownload(episode.id)
                } else {
                    unwatchedDownloaded.add(episode.id)
                }
            }

            // 3. Get all episodes of the series from the server in order
            val seasons = jellyfinApiClient.getSeasons(seriesId)
            val allEpisodes = mutableListOf<BaseItemDto>()
            for (season in seasons) {
                val episodes = jellyfinApiClient.getEpisodesInSeason(seriesId, season.id)
                allEpisodes.addAll(episodes)
            }

            // 4. Find unwatched episodes not already downloaded
            val needed = SMART_DOWNLOAD_COUNT - unwatchedDownloaded.size
            if (needed <= 0) return@withContext

            val toDownload = allEpisodes
                .filter { ep -> ep.userData?.played != true && ep.id !in unwatchedDownloaded }
                .take(needed)
                .map { it.id }

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
        isDirectPlay: Boolean
    ): DownloadRequest {
        val builder = DownloadRequest.Builder(mediaId.toString(), playbackUrl.toUri())
            .setData(title.toByteArray(Charsets.UTF_8))

        if (isDirectPlay) {
            playbackCustomCacheKey(
                mediaId = mediaId.toString(),
                playbackUrl = playbackUrl,
                playMethod = PlayMethod.DIRECT_PLAY
            )?.let(builder::setCustomCacheKey)
        }

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

    private fun formatRuntime(ticks: Long?): String {
        if (ticks == null || ticks <= 0) return "—"
        val totalSeconds = ticks / 10_000_000
        val hours = java.util.concurrent.TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = java.util.concurrent.TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun BaseItemDto.toEpisode(serverUrl: String): Episode {
        return Episode(
            id = id,
            seriesId = seriesId!!,
            seasonId = parentId!!,
            title = name ?: "Unknown title",
            index = indexNumber ?: 0,
            releaseDate = productionYear?.toString() ?: "—",
            rating = officialRating ?: "NR",
            runtime = formatRuntime(runTimeTicks),
            progress = userData?.playedPercentage,
            watched = userData?.played ?: false,
            format = container?.uppercase() ?: "VIDEO",
            synopsis = overview ?: "No synopsis available.",
            imageUrlPrefix = JellyfinImageHelper.toPrefixImageUrl(
                url = serverUrl,
                itemId = id
            ),
            cast = emptyList()
        )
    }

    private fun BaseItemDto.toSeries(serverUrl: String): Series {
        return Series(
            id = id,
            libraryId = parentId ?: java.util.UUID.randomUUID(),
            name = name ?: "Unknown",
            synopsis = overview ?: "No synopsis available",
            year = productionYear?.toString() ?: premiereDate?.year?.toString().orEmpty(),
            imageUrlPrefix = JellyfinImageHelper.toPrefixImageUrl(
                url = serverUrl,
                itemId = id
            ),
            unwatchedEpisodeCount = userData?.unplayedItemCount ?: 0,
            seasonCount = childCount ?: 0,
            seasons = emptyList(),
            cast = emptyList()
        )
    }

    private fun BaseItemDto.toSeason(seriesId: UUID): Season {
        return Season(
            id = id,
            seriesId = this.seriesId ?: seriesId,
            name = name ?: "Unknown",
            index = indexNumber ?: 0,
            unwatchedEpisodeCount = userData?.unplayedItemCount ?: 0,
            episodeCount = childCount ?: 0,
            episodes = emptyList()
        )
    }

    companion object {
        private const val TAG = "MediaDownloadManager"
        private const val SMART_DOWNLOAD_COUNT = 5
    }
}

private fun MediaSourceInfo.shouldUseTranscodingUrl(): Boolean {
    return supportsTranscoding == true &&
        (supportsDirectPlay == false || transcodingUrl != null)
}
