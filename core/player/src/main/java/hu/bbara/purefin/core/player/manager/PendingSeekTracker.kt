package hu.bbara.purefin.core.player.manager

import kotlin.math.abs

internal class PendingSeekTracker(
    private val settleToleranceMs: Long = 500L
) {
    private var pendingSeek: PendingSeek? = null

    fun currentPosition(playerPositionMs: Long): Long {
        val pending = pendingSeek ?: return playerPositionMs
        if (pending.isSatisfiedBy(playerPositionMs, settleToleranceMs)) {
            pendingSeek = null
            return playerPositionMs
        }
        return pending.targetPositionMs
    }

    fun recordSeek(basePositionMs: Long, targetPositionMs: Long): Long {
        val target = targetPositionMs.coerceAtLeast(0L)
        pendingSeek = PendingSeek(
            targetPositionMs = target,
            direction = target.compareTo(basePositionMs)
        )
        return target
    }

    fun clear() {
        pendingSeek = null
    }

    private data class PendingSeek(
        val targetPositionMs: Long,
        val direction: Int
    ) {
        fun isSatisfiedBy(playerPositionMs: Long, settleToleranceMs: Long): Boolean =
            when {
                direction > 0 -> playerPositionMs >= targetPositionMs - settleToleranceMs
                direction < 0 -> playerPositionMs <= targetPositionMs + settleToleranceMs
                else -> abs(playerPositionMs - targetPositionMs) <= settleToleranceMs
            }
    }
}
