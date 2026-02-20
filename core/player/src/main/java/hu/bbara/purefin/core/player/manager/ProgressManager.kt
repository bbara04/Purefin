package hu.bbara.purefin.core.player.manager

import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.core.data.client.JellyfinApiClient
import hu.bbara.purefin.core.data.domain.usecase.UpdateWatchProgressUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class ProgressManager @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val updateWatchProgressUseCase: UpdateWatchProgressUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var activeItemId: UUID? = null
    private var lastPositionMs: Long = 0L
    private var lastDurationMs: Long = 0L
    private var isPaused: Boolean = false

    fun bind(
        playbackState: StateFlow<PlaybackStateSnapshot>,
        progress: StateFlow<PlaybackProgressSnapshot>,
        metadata: StateFlow<MetadataState>
    ) {
        scope.launch {
            combine(playbackState, progress, metadata) { state, prog, meta ->
                Triple(state, prog, meta)
            }.collect { (state, prog, meta) ->
                lastPositionMs = prog.positionMs
                lastDurationMs = prog.durationMs
                isPaused = !state.isPlaying
                val mediaId = meta.mediaId?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                // Media changed or ended - stop session
                if (activeItemId != null && (mediaId != activeItemId || state.isEnded)) {
                    stopSession()
                }

                // Start session when we have a media item and none is active
                if (activeItemId == null && mediaId != null && !state.isEnded) {
                    startSession(mediaId, prog.positionMs)
                }
            }
        }
    }

    private fun startSession(itemId: UUID, positionMs: Long) {
        activeItemId = itemId
        report(itemId, positionMs, isStart = true)
        progressJob = scope.launch {
            while (isActive) {
                delay(5000)
                report(itemId, lastPositionMs, isPaused = isPaused)
            }
        }
    }

    private fun stopSession() {
        progressJob?.cancel()
        activeItemId?.let { itemId ->
            report(itemId, lastPositionMs, isStop = true)
            scope.launch(Dispatchers.IO) {
                try {
                    updateWatchProgressUseCase(itemId, lastPositionMs, lastDurationMs)
                } catch (e: Exception) {
                    Log.e("ProgressManager", "Local cache update failed", e)
                }
            }
        }
        activeItemId = null
    }

    private fun report(itemId: UUID, positionMs: Long, isPaused: Boolean = false, isStart: Boolean = false, isStop: Boolean = false) {
        val ticks = positionMs * 10_000
        scope.launch(Dispatchers.IO) {
            try {
                when {
                    isStart -> jellyfinApiClient.reportPlaybackStart(itemId, ticks)
                    isStop -> jellyfinApiClient.reportPlaybackStopped(itemId, ticks)
                    else -> jellyfinApiClient.reportPlaybackProgress(itemId, ticks, isPaused)
                }
                Log.d("ProgressManager", "${if (isStart) "Start" else if (isStop) "Stop" else "Progress"}: $itemId at ${positionMs}ms, paused=$isPaused")
            } catch (e: Exception) {
                Log.e("ProgressManager", "Report failed", e)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        activeItemId?.let { itemId ->
            val ticks = lastPositionMs * 10_000
            val posMs = lastPositionMs
            val durMs = lastDurationMs
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    jellyfinApiClient.reportPlaybackStopped(itemId, ticks)
                    updateWatchProgressUseCase(itemId, posMs, durMs)
                    Log.d("ProgressManager", "Stop: $itemId at ${posMs}ms")
                } catch (e: Exception) {
                    Log.e("ProgressManager", "Report failed", e)
                }
            }
        }
        scope.cancel()
    }
}
