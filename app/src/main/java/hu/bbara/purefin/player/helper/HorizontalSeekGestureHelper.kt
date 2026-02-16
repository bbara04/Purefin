package hu.bbara.purefin.player.helper

import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.pow

internal object HorizontalSeekGestureHelper {
    val START_THRESHOLD = 12.dp
    private const val COEFFICIENT = 3.1f
    const val EXPONENT = 1.7f
    private const val MAX_DELTA_MS = 12_000_000L

    fun deltaMs(rawDelta: Float): Long {
        val magnitude = abs(rawDelta)
        if (magnitude == 0f) return 0L
        val magnitudePow = magnitude.toDouble().pow(EXPONENT.toDouble()).toFloat()
        val scaled = COEFFICIENT * magnitudePow
        val signed = if (rawDelta > 0f) scaled else -scaled
        return signed.toLong().coerceIn(-MAX_DELTA_MS, MAX_DELTA_MS)
    }
}