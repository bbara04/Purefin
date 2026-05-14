package hu.bbara.purefin.data.jellyfin.playback

import android.media.MediaCodecList
import timber.log.Timber

/**
 * Helper to debug available audio/video codecs on the device.
 */
object CodecDebugHelper {

    private const val TAG = "CodecDebug"

    /**
     * Logs all available decoders on this device.
     * Call this to understand what your device can actually decode.
     */
    fun logAvailableDecoders() {
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            Timber.tag(TAG).d("=== Available Audio Decoders ===")

            codecList.codecInfos
                .filter { !it.isEncoder }
                .forEach { codecInfo ->
                    codecInfo.supportedTypes.forEach { mimeType ->
                        if (mimeType.startsWith("audio/")) {
                            Timber.tag(TAG).d("${codecInfo.name}: $mimeType")
                            if (mimeType.contains("dts", ignoreCase = true) ||
                                mimeType.contains("truehd", ignoreCase = true)) {
                                Timber.tag(TAG).w("  ^^^ DTS/TrueHD decoder found! ^^^")
                            }
                        }
                    }
                }

            Timber.tag(TAG).d("=== Available Video Decoders ===")
            codecList.codecInfos
                .filter { !it.isEncoder }
                .forEach { codecInfo ->
                    codecInfo.supportedTypes.forEach { mimeType ->
                        if (mimeType.startsWith("video/")) {
                            Timber.tag(TAG).d("${codecInfo.name}: $mimeType")
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to list codecs")
        }
    }

    /**
     * Check if a specific MIME type has a decoder available.
     */
    fun hasDecoderFor(mimeType: String): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            codecList.codecInfos.any { codecInfo ->
                !codecInfo.isEncoder &&
                codecInfo.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
            }
        } catch (e: Exception) {
            false
        }
    }
}
