package hu.bbara.purefin.player.manager

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import hu.bbara.purefin.player.model.TrackOption
import hu.bbara.purefin.player.model.TrackType
import java.util.Locale
import javax.inject.Inject

data class TrackSelectionState(
    val audioTracks: List<TrackOption> = emptyList(),
    val textTracks: List<TrackOption> = emptyList(),
    val videoTracks: List<TrackOption> = emptyList(),
    val selectedAudioTrackId: String? = null,
    val selectedTextTrackId: String? = null,
    val selectedVideoTrackId: String? = null
)

class TrackMapper @Inject constructor() {

    @OptIn(UnstableApi::class)
    fun map(tracks: Tracks): TrackSelectionState {
        val audio = mutableListOf<TrackOption>()
        val text = mutableListOf<TrackOption>()
        val video = mutableListOf<TrackOption>()
        var selectedAudio: String? = null
        var selectedText: String? = null
        var selectedVideo: String? = null

        tracks.groups.forEachIndexed { groupIndex, group ->
            when (group.type) {
                C.TRACK_TYPE_AUDIO -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "a_${groupIndex}_${trackIndex}"
                        val label = formatAudioLabel(format, trackIndex)
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = format.language,
                            bitrate = format.bitrate,
                            channelCount = format.channelCount,
                            height = null,
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.AUDIO,
                            isOff = false
                        )
                        audio.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedAudio = id
                    }
                }

                C.TRACK_TYPE_TEXT -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "t_${groupIndex}_${trackIndex}"
                        val isForced = (format.selectionFlags and C.SELECTION_FLAG_FORCED) != 0
                        val label = formatTextLabel(format, trackIndex, isForced)
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = format.language,
                            bitrate = null,
                            channelCount = null,
                            height = null,
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.TEXT,
                            isOff = false,
                            forced = isForced
                        )
                        text.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedText = id
                    }
                }

                C.TRACK_TYPE_VIDEO -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "v_${groupIndex}_${trackIndex}"
                        val label = formatVideoLabel(format, trackIndex)
                        val option = TrackOption(
                            id = id,
                            label = label,
                            language = null,
                            bitrate = format.bitrate,
                            channelCount = null,
                            height = format.height.takeIf { it > 0 },
                            groupIndex = groupIndex,
                            trackIndex = trackIndex,
                            type = TrackType.VIDEO,
                            isOff = false
                        )
                        video.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedVideo = id
                    }
                }
            }
        }

        if (text.isNotEmpty()) {
            text.add(
                0,
                TrackOption(
                    id = "text_off",
                    label = "Off",
                    language = null,
                    bitrate = null,
                    channelCount = null,
                    height = null,
                    groupIndex = -1,
                    trackIndex = -1,
                    type = TrackType.TEXT,
                    isOff = true
                )
            )
        }

        return TrackSelectionState(
            audioTracks = audio,
            textTracks = text,
            videoTracks = video,
            selectedAudioTrackId = selectedAudio,
            selectedTextTrackId = selectedText ?: text.firstOrNull { option -> option.isOff }?.id,
            selectedVideoTrackId = selectedVideo
        )
    }

    private fun formatAudioLabel(format: Format, trackIndex: Int): String {
        val details = listOfNotNull(
            displayLanguage(format.language),
            audioCodec(format),
            channelLabel(format.channelCount)
        ).joinToString(" ")

        return labelWithDetails(
            title = format.label,
            details = details,
            fallback = "Audio ${trackIndex + 1}"
        )
    }

    private fun formatTextLabel(format: Format, trackIndex: Int, isForced: Boolean): String {
        val details = listOfNotNull(
            displayLanguage(format.language),
            subtitleCodec(format),
            "Forced".takeIf { isForced }
        ).joinToString(" ")

        return labelWithDetails(
            title = format.label,
            details = details,
            fallback = "Subtitle ${trackIndex + 1}"
        )
    }

    private fun formatVideoLabel(format: Format, trackIndex: Int): String {
        val details = if (format.height != Format.NO_VALUE && format.height > 0) {
            "${format.height}p"
        } else {
            null
        }

        return labelWithDetails(
            title = format.label,
            details = details,
            fallback = "Video ${trackIndex + 1}"
        )
    }

    private fun labelWithDetails(title: String?, details: String?, fallback: String): String {
        val cleanTitle = title?.trim()?.takeIf { it.isNotEmpty() }
        val cleanDetails = details?.trim()?.takeIf { it.isNotEmpty() }

        return when {
            cleanTitle != null && cleanDetails != null && cleanTitle != cleanDetails ->
                "$cleanTitle - $cleanDetails"

            cleanTitle != null -> cleanTitle
            cleanDetails != null -> cleanDetails
            else -> fallback
        }
    }

    private fun displayLanguage(code: String?): String? {
        val cleanCode = code
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("und", ignoreCase = true) }
            ?: return null
        val locale = Locale.forLanguageTag(cleanCode.replace('_', '-'))
        val language = locale.getDisplayLanguage(Locale.getDefault())

        return language.takeIf { it.isNotEmpty() && it != cleanCode } ?: cleanCode
    }

    private fun audioCodec(format: Format): String? {
        val value = format.codecText()

        return when {
            value.contains("truehd") -> "TrueHD"
            value.contains("eac3") || value.contains("e-ac-3") || value.contains("ec-3") ->
                "Dolby Digital Plus"

            value.contains("ac3") || value.contains("ac-3") -> "Dolby Digital"
            value.contains("dts") || value.contains("dca") -> "DTS"
            value.contains("alac") -> "ALAC"
            value.contains("flac") -> "FLAC"
            value.contains("opus") -> "Opus"
            value.contains("vorbis") || value.contains("ogg") -> "Vorbis"
            value.contains("mp3") || value.contains("mpeg") -> "MP3"
            value.contains("aac") || value.contains("mp4a") -> "AAC"
            value.contains("pcm") || value.contains("raw") || value.contains("lpcm") -> "PCM"
            else -> null
        }
    }

    private fun subtitleCodec(format: Format): String? {
        val value = format.codecText()

        return when {
            value.contains("subrip") || value.contains("srt") -> "SRT"
            value.contains("ssa") || value.contains("ass") -> "ASS"
            value.contains("pgs") || value.contains("pgssub") -> "PGS"
            value.contains("dvb") -> "DVB"
            value.contains("dvd") || value.contains("vobsub") -> "DVD"
            value.contains("vtt") || value.contains("webvtt") -> "VTT"
            else -> null
        }
    }

    private fun Format.codecText(): String = listOfNotNull(
        sampleMimeType,
        containerMimeType,
        codecs
    ).joinToString(" ").lowercase(Locale.US)

    private fun channelLabel(channelCount: Int): String? =
        when {
            channelCount == Format.NO_VALUE || channelCount <= 0 -> null
            channelCount == 1 -> "Mono"
            channelCount == 2 -> "Stereo"
            channelCount == 6 -> "5.1"
            channelCount == 8 -> "7.1"
            else -> "$channelCount ch"
        }
}
