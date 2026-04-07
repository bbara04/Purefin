package hu.bbara.purefin.core.data.client

internal data class HlsStabilityOptions(
    val segmentLengthSeconds: Int,
    val minSegments: Int,
    val breakOnNonKeyFrames: Boolean,
    val copyTimestamps: Boolean
)

internal object HlsPlaybackStability {
    val conservativeHlsOptions = HlsStabilityOptions(
        segmentLengthSeconds = 6,
        minSegments = 3,
        breakOnNonKeyFrames = false,
        copyTimestamps = true
    )
}
