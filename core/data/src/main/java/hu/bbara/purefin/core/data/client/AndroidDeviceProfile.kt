package hu.bbara.purefin.core.data.client

import android.content.Context
import android.media.AudioAttributes as PlatformAudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodecList
import android.util.Log
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.DirectPlayProfile
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.EncodingContext
import org.jellyfin.sdk.model.api.MediaStreamProtocol
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.api.SubtitleProfile
import org.jellyfin.sdk.model.api.TranscodingProfile

/**
 * Creates a DeviceProfile for Android devices with proper codec support detection.
 * This prevents playback failures by requesting transcoding for unsupported formats like DTS-HD.
 */
object AndroidDeviceProfile {

    private const val TAG = "AndroidDeviceProfile"
    private const val DEFAULT_MAX_AUDIO_CHANNELS = 2
    private const val PROBE_SAMPLE_RATE = 48_000
    private const val PROBE_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    @Volatile
    private var cachedSnapshot: CapabilitySnapshot? = null

    data class CapabilitySnapshot(
        val deviceProfile: DeviceProfile,
        val maxAudioChannels: Int
    )

    fun getSnapshot(context: Context): CapabilitySnapshot {
        cachedSnapshot?.let { return it }

        return synchronized(this) {
            cachedSnapshot?.let { return@synchronized it }

            val applicationContext = context.applicationContext

            // Debug logging is noisy and expensive, so keep it to debug builds/devices only when needed.
            val audioCodecs = getAudioCodecs()
            val videoCodecs = getVideoCodecs()
            val maxAudioChannels = resolveMaxAudioChannels(applicationContext)

            Log.d(TAG, "Supported audio codecs: ${audioCodecs.joinToString()}")
            Log.d(TAG, "Supported video codecs: ${videoCodecs.joinToString()}")
            Log.d(TAG, "Max audio channels: $maxAudioChannels")

            val snapshot = CapabilitySnapshot(
                deviceProfile = createDeviceProfile(audioCodecs, videoCodecs, maxAudioChannels),
                maxAudioChannels = maxAudioChannels
            )
            cachedSnapshot = snapshot
            snapshot
        }
    }

    fun create(context: Context): DeviceProfile = getSnapshot(context).deviceProfile

    internal fun createDeviceProfile(
        audioCodecs: List<String>,
        videoCodecs: List<String>,
        maxAudioChannels: Int
    ): DeviceProfile {
        return DeviceProfile(
            name = "Android Media3",
            maxStaticBitrate = 100_000_000,
            maxStreamingBitrate = 100_000_000,

            // Direct play profiles - what we can play natively
            directPlayProfiles = listOf(
                DirectPlayProfile(
                    type = DlnaProfileType.VIDEO,
                    container = "mp4,m4v,mkv,webm,ts,mov",
                    videoCodec = videoCodecs.joinToString(","),
                    audioCodec = audioCodecs.joinToString(",")
                )
            ),

            // Explicit video transcoding support keeps Jellyfin 10.10/10.11 from
            // returning sources that exist but are not marked usable for playback.
            transcodingProfiles = listOf(
                TranscodingProfile(
                    container = "ts",
                    type = DlnaProfileType.VIDEO,
                    videoCodec = "h264",
                    audioCodec = "aac",
                    protocol = MediaStreamProtocol.HLS,
                    context = EncodingContext.STREAMING,
                    maxAudioChannels = maxAudioChannels.toString(),
                    minSegments = 2,
                    breakOnNonKeyFrames = true,
                    conditions = emptyList()
                )
            ),

            codecProfiles = emptyList(),

            subtitleProfiles = listOf(
                // Prefer EMBED so subtitles stay in the container — this gives
                // correct cues after seeking (Media3 parses them at extraction time).
                SubtitleProfile("srt", SubtitleDeliveryMethod.EMBED),
                SubtitleProfile("ass", SubtitleDeliveryMethod.EMBED),
                SubtitleProfile("ssa", SubtitleDeliveryMethod.EMBED),
                SubtitleProfile("subrip", SubtitleDeliveryMethod.EMBED),
                SubtitleProfile("sub", SubtitleDeliveryMethod.EMBED),
                // EXTERNAL fallback for when embedding isn't possible
                SubtitleProfile("srt", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("ass", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("ssa", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("vtt", SubtitleDeliveryMethod.EXTERNAL),
                SubtitleProfile("sub", SubtitleDeliveryMethod.EXTERNAL)
            ),

            containerProfiles = emptyList()
        )
    }

    /**
     * Get list of supported audio codecs on this device.
     * Excludes unsupported formats like DTS, DTS-HD, TrueHD which commonly cause playback failures.
     */
    private fun getAudioCodecs(): List<String> {
        val supportedCodecs = mutableListOf<String>()

        val commonCodecs = listOf(
            "aac" to "audio/mp4a-latm",
            "mp3" to "audio/mpeg",
            "ac3" to "audio/ac3",
            "eac3" to "audio/eac3",
            "dts" to "audio/vnd.dts",
            "dtshd_ma" to "audio/vnd.dts.hd",
            "truehd" to "audio/true-hd",
            "flac" to "audio/flac",
            "vorbis" to "audio/vorbis",
            "opus" to "audio/opus",
            "alac" to "audio/alac"
        )

        for ((codecName, mimeType) in commonCodecs) {
            if (isCodecSupported(mimeType)) {
                supportedCodecs.add(codecName)
            }
        }

        // AAC is mandatory on Android - ensure it's always included
        if (!supportedCodecs.contains("aac")) {
            supportedCodecs.add("aac")
        }

        return supportedCodecs
    }

    /**
     * Get list of supported video codecs on this device.
     */
    private fun getVideoCodecs(): List<String> {
        val supportedCodecs = mutableListOf<String>()

        val commonCodecs = listOf(
            "h264" to "video/avc",
            "hevc" to "video/hevc",
            "vp9" to "video/x-vnd.on2.vp9",
            "vp8" to "video/x-vnd.on2.vp8",
            "mpeg4" to "video/mp4v-es",
            "av1" to "video/av01"
        )

        for ((codecName, mimeType) in commonCodecs) {
            if (isCodecSupported(mimeType)) {
                supportedCodecs.add(codecName)
            }
        }

        // H.264 is mandatory on Android - ensure it's always included
        if (!supportedCodecs.contains("h264")) {
            supportedCodecs.add("h264")
        }

        return supportedCodecs
    }

    /**
     * Check if a specific decoder (not encoder) is supported on this device.
     */
    private fun isCodecSupported(mimeType: String): Boolean {
        val platformSupport = try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            codecList.codecInfos.any { codecInfo ->
                !codecInfo.isEncoder &&
                codecInfo.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
            }
        } catch (_: Exception) {
            false
        }

        if (platformSupport) {
            return true
        }

        return isCodecSupportedByFfmpeg(mimeType)
    }

    private fun isCodecSupportedByFfmpeg(mimeType: String): Boolean {
        return runCatching {
            val ffmpegLibraryClass = Class.forName("androidx.media3.decoder.ffmpeg.FfmpegLibrary")
            val supportsFormat = ffmpegLibraryClass.getMethod("supportsFormat", String::class.java)
            supportsFormat.invoke(null, mimeType) as? Boolean ?: false
        }.getOrElse { false }
    }

    private fun resolveMaxAudioChannels(context: Context): Int {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return DEFAULT_MAX_AUDIO_CHANNELS
        return runCatching {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val reportedMaxChannels = devices
                .flatMap { device ->
                    buildList {
                        addAll(device.channelCounts.toList())
                        addAll(device.channelMasks.map(::channelMaskToCount))
                        addAll(device.channelIndexMasks.map { Integer.bitCount(it) })
                    }
                }
                .maxOrNull()
                ?.takeIf { it > 0 }
                ?: DEFAULT_MAX_AUDIO_CHANNELS

            val verifiedMaxChannels = verifyOutputChannelSupport(reportedMaxChannels)
            Log.d(TAG, "Reported max audio channels: $reportedMaxChannels, verified: $verifiedMaxChannels")
            verifiedMaxChannels
        }.getOrElse {
            DEFAULT_MAX_AUDIO_CHANNELS
        }
    }

    private fun verifyOutputChannelSupport(reportedMaxChannels: Int): Int {
        val candidates = buildList {
            if (reportedMaxChannels >= 8) add(8)
            if (reportedMaxChannels >= 6) add(6)
            add(2)
        }.distinct()

        return candidates.firstOrNull(::canCreateAudioTrackForChannelCount) ?: DEFAULT_MAX_AUDIO_CHANNELS
    }

    private fun canCreateAudioTrackForChannelCount(channelCount: Int): Boolean {
        val channelMask = when (channelCount) {
            8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
            6 -> AudioFormat.CHANNEL_OUT_5POINT1
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> return false
        }

        val minBufferSize = AudioTrack.getMinBufferSize(PROBE_SAMPLE_RATE, channelMask, PROBE_ENCODING)
        if (minBufferSize <= 0) {
            return false
        }

        return runCatching {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    PlatformAudioAttributes.Builder()
                        .setUsage(PlatformAudioAttributes.USAGE_MEDIA)
                        .setContentType(PlatformAudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(PROBE_SAMPLE_RATE)
                        .setEncoding(PROBE_ENCODING)
                        .setChannelMask(channelMask)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBufferSize * 2)
                .build()

            try {
                audioTrack.state == AudioTrack.STATE_INITIALIZED
            } finally {
                audioTrack.release()
            }
        }.getOrElse { false }
    }

    private fun channelMaskToCount(mask: Int): Int {
        return if (mask == 0) {
            0
        } else {
            Integer.bitCount(mask)
        }
    }
}
