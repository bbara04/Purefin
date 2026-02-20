package hu.bbara.purefin.core.player.preference

import kotlinx.serialization.Serializable

@Serializable
data class TrackPreferences(
    val mediaPreferences: Map<String, MediaTrackPreferences> = emptyMap()
)

@Serializable
data class MediaTrackPreferences(
    val mediaId: String,
    val audioPreference: AudioTrackProperties? = null,
    val subtitlePreference: SubtitleTrackProperties? = null
)

@Serializable
data class AudioTrackProperties(
    val language: String? = null,
    val channelCount: Int? = null,
    val label: String? = null
)

@Serializable
data class SubtitleTrackProperties(
    val language: String? = null,
    val forced: Boolean = false,
    val label: String? = null,
    val isOff: Boolean = false
)
