package hu.bbara.purefin.player.manager

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import hu.bbara.purefin.model.MediaSegment
import hu.bbara.purefin.player.model.SegmentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaSegmentManager(private val player: ExoPlayer) {

    private var listener: MediaSegmentListener? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSegments: List<MediaSegment> = emptyList()
    private var activeSegment: MediaSegment? = null

    interface MediaSegmentListener {
        fun onEvent(mediaSegment: MediaSegment, status: SegmentStatus)
    }

    init {
        startPolling()
    }

    @Synchronized
    fun registerListener(listener: MediaSegmentListener) {
        this.listener?.let {
            Log.w("MediaSegmentManager", "Listener was already register")
            return
        }
        this.listener = listener
    }

    fun addMediaSegments(mediaSegments: List<MediaSegment>) {
        this.mediaSegments = mediaSegments
        activeSegment = null
    }

    fun release() {
        scope.cancel()
    }

    private fun startPolling() {
        scope.launch {
            while (isActive) {
                updateActiveSegment()
                delay(1_000)
            }
        }
    }

    private fun updateActiveSegment() {
        val currentPosition = player.currentPosition
        val currentSegment = mediaSegments.firstOrNull { mediaSegment ->
            currentPosition >= mediaSegment.startMs && currentPosition < mediaSegment.endMs
        }
        val previousSegment = activeSegment
        if (previousSegment?.id == currentSegment?.id) return

        previousSegment?.let {
            notifyListener(it, SegmentStatus.END)
        }
        currentSegment?.let {
            notifyListener(it, SegmentStatus.START)
        }
        activeSegment = currentSegment
    }

    private fun notifyListener(mediaSegment: MediaSegment, status: SegmentStatus) {
        val listener = listener
        if (listener == null) {
            Log.w("MediaSegmentManager", "Listener was not register therefore it cannot notify")
            return
        }
        Log.d("MediaSegmentManager", "Notify listener about $mediaSegment with status $status")
        listener.onEvent(mediaSegment, status)
    }
}
