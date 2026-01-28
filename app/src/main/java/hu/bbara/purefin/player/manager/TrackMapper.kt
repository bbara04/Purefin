package hu.bbara.purefin.player.manager

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import hu.bbara.purefin.player.model.TrackOption
import hu.bbara.purefin.player.model.TrackType
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
                        val label = format.label
                            ?: format.language
                            ?: "${format.channelCount}ch"
                            ?: "Audio ${trackIndex}"
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
                        val label = format.label
                            ?: format.language
                            ?: "Subtitle ${trackIndex}"
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
                            isOff = false
                        )
                        text.add(option)
                        if (group.isTrackSelected(trackIndex)) selectedText = id
                    }
                }

                C.TRACK_TYPE_VIDEO -> {
                    repeat(group.length) { trackIndex ->
                        val format = group.getTrackFormat(trackIndex)
                        val id = "v_${groupIndex}_${trackIndex}"
                        val res = if (format.height != Format.NO_VALUE) "${format.height}p" else null
                        val label = res ?: format.label ?: "Video ${trackIndex}"
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
}
