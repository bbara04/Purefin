package hu.bbara.purefin.player.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SeekByCollector(
    private val scope: CoroutineScope,
    private val intervalMs: Long = 120,
    private val seekBy: (Long) -> Unit,
) {
    private var pendingSeekMs = 0L
    private var flushJob: Job? = null

    fun seekBySoon(deltaMs: Long) {
        pendingSeekMs += deltaMs
        if (flushJob?.isActive != true) {
            scheduleFlush()
        }
    }

    fun clear() {
        pendingSeekMs = 0
        flushJob?.cancel()
        flushJob = null
    }

    private fun scheduleFlush() {
        flushJob = scope.launch {
            delay(intervalMs)
            flush()
        }
    }

    private fun flush() {
        val deltaMs = pendingSeekMs
        pendingSeekMs = 0
        if (deltaMs != 0L) {
            seekBy(deltaMs)
        }
        if (pendingSeekMs != 0L) {
            scheduleFlush()
        } else {
            flushJob = null
        }
    }
}
