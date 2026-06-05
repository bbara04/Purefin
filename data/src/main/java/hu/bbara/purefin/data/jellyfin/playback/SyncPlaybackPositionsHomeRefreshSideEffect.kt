package hu.bbara.purefin.data.jellyfin.playback

import hu.bbara.purefin.core.Offline
import hu.bbara.purefin.core.Online
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.feature.browse.home.refresh.HomeRefreshSideEffect
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Movie
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToLong

class SyncPlaybackPositionsHomeRefreshSideEffect @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val jellyfinApiClient: JellyfinApiClient,
    @param:Offline private val offlineRepository: LocalMediaRepository,
    @param:Online private val onlineRepository: LocalMediaRepository,
) : HomeRefreshSideEffect {

    override suspend fun run() {
        if (!networkMonitor.isOnline.first()) return

        val localPlaybackPositions = buildList {
            addAll(offlineRepository.movies.first().values.map { it.toLocalPlaybackPosition() })
            addAll(offlineRepository.episodes.first().values.map { it.toLocalPlaybackPosition() })
        }

        localPlaybackPositions.forEach { localPlaybackPosition ->
            syncPlaybackPosition(localPlaybackPosition)
        }
    }

    private suspend fun syncPlaybackPosition(localPlaybackPosition: LocalPlaybackPosition) {
        try {
            val remoteItem = jellyfinApiClient.getItemInfo(localPlaybackPosition.mediaId) ?: return
            val remotePlaybackPosition = remoteItem.remotePlaybackPosition() ?: return
            val localPlaybackPositionTicks = localPlaybackPosition.toPlaybackPositionTicks(
                runtimeTicks = remotePlaybackPosition.runtimeTicks,
            )
            val tickTolerance = remotePlaybackPosition.runtimeTicks.progressToleranceTicks()

            // Offline Room stores progress percent without last-updated metadata, so sync the
            // furthest known playback position as the best available source of truth.
            when {
                localPlaybackPositionTicks > remotePlaybackPosition.playbackPositionTicks + tickTolerance -> {
                    jellyfinApiClient.updatePlaybackPosition(
                        mediaId = localPlaybackPosition.mediaId,
                        playbackPositionTicks = localPlaybackPositionTicks,
                        runtimeTicks = remotePlaybackPosition.runtimeTicks,
                    )
                    onlineRepository.updateWatchProgressPercent(
                        mediaId = localPlaybackPosition.mediaId,
                        progressPercent = localPlaybackPositionTicks.toProgressPercent(
                            runtimeTicks = remotePlaybackPosition.runtimeTicks,
                        ),
                    )
                }
                remotePlaybackPosition.playbackPositionTicks > localPlaybackPositionTicks + tickTolerance -> {
                    val remoteProgressPercent = remotePlaybackPosition.playbackPositionTicks.toProgressPercent(
                        runtimeTicks = remotePlaybackPosition.runtimeTicks,
                    )
                    offlineRepository.updateWatchProgressPercent(
                        mediaId = localPlaybackPosition.mediaId,
                        progressPercent = remoteProgressPercent,
                    )
                    onlineRepository.updateWatchProgressPercent(
                        mediaId = localPlaybackPosition.mediaId,
                        progressPercent = remoteProgressPercent,
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            Timber.tag(TAG).w(error, "Unable to sync playback position for ${localPlaybackPosition.mediaId}")
        }
    }

    private fun Movie.toLocalPlaybackPosition(): LocalPlaybackPosition =
        LocalPlaybackPosition(
            mediaId = id,
            progressPercent = progress.toLocalProgressPercent(watched),
        )

    private fun Episode.toLocalPlaybackPosition(): LocalPlaybackPosition =
        LocalPlaybackPosition(
            mediaId = id,
            progressPercent = progress.toLocalProgressPercent(watched),
        )

    private fun Double?.toLocalProgressPercent(watched: Boolean): Double {
        if (watched) return 100.0
        return this?.normalizedProgressPercent() ?: 0.0
    }

    private fun BaseItemDto.remotePlaybackPosition(): RemotePlaybackPosition? {
        val runtimeTicks = runTimeTicks?.takeIf { it > 0L } ?: return null
        val userData = userData ?: return null
        val userPlaybackPositionTicks = userData.playbackPositionTicks
        val playbackPositionTicks = when {
            userData.played -> runtimeTicks
            userPlaybackPositionTicks != null -> userPlaybackPositionTicks
            else -> userData.playedPercentage?.toPlaybackPositionTicks(runtimeTicks) ?: 0L
        }

        return RemotePlaybackPosition(
            playbackPositionTicks = playbackPositionTicks.coerceIn(0L, runtimeTicks),
            runtimeTicks = runtimeTicks,
        )
    }

    private fun LocalPlaybackPosition.toPlaybackPositionTicks(runtimeTicks: Long): Long {
        return progressPercent.toPlaybackPositionTicks(runtimeTicks)
    }

    private fun Double.normalizedProgressPercent(): Double? {
        if (isNaN()) return null
        return coerceIn(0.0, 100.0)
    }

    private fun Double.toPlaybackPositionTicks(runtimeTicks: Long): Long {
        val progressPercent = normalizedProgressPercent() ?: 0.0
        return ((progressPercent / 100.0) * runtimeTicks.toDouble())
            .roundToLong()
            .coerceIn(0L, runtimeTicks)
    }

    private fun Long.toProgressPercent(runtimeTicks: Long): Double {
        if (runtimeTicks <= 0L) return 0.0
        return ((toDouble() / runtimeTicks.toDouble()) * 100.0).coerceIn(0.0, 100.0)
    }

    private fun Long.progressToleranceTicks(): Long {
        return ((toDouble() * PROGRESS_TOLERANCE_PERCENT) / 100.0).roundToLong()
    }

    private data class LocalPlaybackPosition(
        val mediaId: UUID,
        val progressPercent: Double,
    )

    private data class RemotePlaybackPosition(
        val playbackPositionTicks: Long,
        val runtimeTicks: Long,
    )

    private companion object {
        const val TAG = "PlaybackPositionSync"
        const val PROGRESS_TOLERANCE_PERCENT = 0.5
    }
}
