package hu.bbara.purefin.core.player.manager

import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import hu.bbara.purefin.core.data.MediaProgressWriter
import hu.bbara.purefin.core.data.PlaybackProgressReporter
import hu.bbara.purefin.core.data.PlaybackReportContext
import hu.bbara.purefin.core.player.model.MetadataState
import hu.bbara.purefin.core.player.model.PlaybackProgressSnapshot
import hu.bbara.purefin.core.player.model.PlaybackStateSnapshot
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
    private val playbackProgressReporter: PlaybackProgressReporter,
    private val mediaProgressWriter: MediaProgressWriter,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var activeItemId: UUID? = null
    private var activePlaybackReportContext: PlaybackReportContext? = null
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
                val nextPlaybackReportContext = meta.playbackReportContext

                if (activeItemId != null && (mediaId != activeItemId || state.isEnded)) {
                    stopSession()
                }

                if (activeItemId == null && mediaId != null && !state.isEnded) {
                    startSession(mediaId, prog.positionMs, nextPlaybackReportContext)
                } else if (activeItemId == mediaId) {
                    activePlaybackReportContext = nextPlaybackReportContext
                }
            }
        }
    }

    private fun startSession(itemId: UUID, positionMs: Long, reportContext: PlaybackReportContext?) {
        activeItemId = itemId
        activePlaybackReportContext = reportContext
        report(itemId, positionMs, reportContext = reportContext, isStart = true)
        progressJob = scope.launch {
            while (isActive) {
                delay(5000)
                report(itemId, lastPositionMs, reportContext = activePlaybackReportContext, isPaused = isPaused)
            }
        }
    }

    private fun stopSession() {
        progressJob?.cancel()
        activeItemId?.let { itemId ->
            report(itemId, lastPositionMs, reportContext = activePlaybackReportContext, isStop = true)
            scope.launch(Dispatchers.IO) {
                try {
                    mediaProgressWriter.updateWatchProgress(itemId, lastPositionMs, lastDurationMs)
                } catch (e: Exception) {
                    Log.e("ProgressManager", "Local cache update failed", e)
                }
            }
        }
        activeItemId = null
        activePlaybackReportContext = null
    }

    private fun report(
        itemId: UUID,
        positionMs: Long,
        reportContext: PlaybackReportContext?,
        isPaused: Boolean = false,
        isStart: Boolean = false,
        isStop: Boolean = false
    ) {
        val ticks = positionMs * 10_000
        scope.launch(Dispatchers.IO) {
            try {
                if (reportContext == null) {
                    return@launch
                }
                when {
                    isStart -> playbackProgressReporter.reportPlaybackStart(itemId, ticks, reportContext)
                    isStop -> playbackProgressReporter.reportPlaybackStopped(itemId, ticks, reportContext)
                    else -> playbackProgressReporter.reportPlaybackProgress(itemId, ticks, isPaused, reportContext)
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
                    activePlaybackReportContext?.let { reportContext ->
                        playbackProgressReporter.reportPlaybackStopped(itemId, ticks, reportContext)
                    }
                    mediaProgressWriter.updateWatchProgress(itemId, posMs, durMs)
                    Log.d("ProgressManager", "Stop: $itemId at ${posMs}ms")
                } catch (e: Exception) {
                    Log.e("ProgressManager", "Report failed", e)
                }
            }
        }
        scope.cancel()
    }
}
