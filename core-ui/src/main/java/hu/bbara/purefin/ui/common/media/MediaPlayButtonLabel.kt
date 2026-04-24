package hu.bbara.purefin.ui.common.media

fun mediaPlayButtonText(progressPercent: Double?, watched: Boolean): String {
    return if ((progressPercent ?: 0.0) > 0.0 && !watched) "Resume" else "Play"
}

fun mediaPlaybackProgress(progressPercent: Double?): Float {
    return progressPercent?.div(100)?.toFloat() ?: 0f
}
