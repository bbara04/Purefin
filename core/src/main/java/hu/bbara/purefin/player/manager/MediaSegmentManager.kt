package hu.bbara.purefin.player.manager

import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import hu.bbara.purefin.model.MediaSegment
import hu.bbara.purefin.model.SegmentType
import hu.bbara.purefin.player.model.SegmentStatus

@OptIn(UnstableApi::class)
class MediaSegmentManager(private val player: ExoPlayer) {

    private var listener: MediaSegmentListener? = null

    interface MediaSegmentListener {
        fun onEvent(segmentType: SegmentType, status: SegmentStatus)
    }

    @Synchronized
    fun registerListener(listener: MediaSegmentListener) {
        this.listener?.let {
            Log.w("PlayerMessageManager", "Listener was already register")
            return
        }
        this.listener = listener
    }

    fun addMediaSegments(mediaSegments: List<MediaSegment>) {
        val messageTarget = PlayerMessage.Target { messageType, payload ->
            SegmentType.fromValue(messageType)?.let { segmentType ->
                notifyListener(segmentType, payload as SegmentStatus)
            }
        }

        mediaSegments.forEach { mediaSegment ->
            installMediaSegment(messageTarget, mediaSegment)
        }

    }

    private fun notifyListener(segmentType: SegmentType, status: SegmentStatus) {
        if (listener == null) {
            Log.w("PlayerMessageManager", "Listener was not register therefore it cannot notify")
        }
        listener?.onEvent(segmentType, status)

    }

    private fun installMediaSegment(messageTarget: PlayerMessage.Target, mediaSegment: MediaSegment) {
        player.createMessage(messageTarget)
            .setType(mediaSegment.type.value)
            .setPosition(mediaSegment.startMs)
            .setPayload(SegmentStatus.START)
            .setLooper(Looper.getMainLooper())
            .setDeleteAfterDelivery(false)
            .send()

        player.createMessage(messageTarget)
            .setType(mediaSegment.type.value)
            .setPayload(SegmentStatus.END)
            .setPosition(mediaSegment.endMs)
            .setLooper(Looper.getMainLooper())
            .setDeleteAfterDelivery(false)
            .send()
    }
}