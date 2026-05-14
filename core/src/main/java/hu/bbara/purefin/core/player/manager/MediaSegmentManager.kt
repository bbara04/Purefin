package hu.bbara.purefin.core.player.manager

import androidx.media3.exoplayer.ExoPlayer
import hu.bbara.purefin.core.player.model.SegmentStatus
import hu.bbara.purefin.model.MediaSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

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
            Timber.tag(TAG).w("Listener was already register")
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
            Timber.tag(TAG).w("Listener was not register therefore it cannot notify")
            return
        }
        Timber.tag(TAG).d("Notify listener about $mediaSegment with status $status")
        listener.onEvent(mediaSegment, status)
    }

    private companion object {
        const val TAG = "MediaSegmentManager"
    }
}
