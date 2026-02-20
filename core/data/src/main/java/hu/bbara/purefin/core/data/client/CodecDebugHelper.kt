package hu.bbara.purefin.core.data.client

import android.media.MediaCodecList
import android.util.Log

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
            Log.d(TAG, "=== Available Audio Decoders ===")

            codecList.codecInfos
                .filter { !it.isEncoder }
                .forEach { codecInfo ->
                    codecInfo.supportedTypes.forEach { mimeType ->
                        if (mimeType.startsWith("audio/")) {
                            Log.d(TAG, "${codecInfo.name}: $mimeType")
                            if (mimeType.contains("dts", ignoreCase = true) ||
                                mimeType.contains("truehd", ignoreCase = true)) {
                                Log.w(TAG, "  ^^^ DTS/TrueHD decoder found! ^^^")
                            }
                        }
                    }
                }

            Log.d(TAG, "=== Available Video Decoders ===")
            codecList.codecInfos
                .filter { !it.isEncoder }
                .forEach { codecInfo ->
                    codecInfo.supportedTypes.forEach { mimeType ->
                        if (mimeType.startsWith("video/")) {
                            Log.d(TAG, "${codecInfo.name}: $mimeType")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list codecs", e)
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
