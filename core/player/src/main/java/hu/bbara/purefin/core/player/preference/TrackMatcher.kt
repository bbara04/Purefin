package hu.bbara.purefin.core.player.preference

import hu.bbara.purefin.core.player.model.TrackOption
import hu.bbara.purefin.core.player.model.TrackType
import javax.inject.Inject

class TrackMatcher @Inject constructor() {

    /**
     * Finds the best matching audio track based on stored preferences.
     * Scoring: language (3) + channelCount (2) + label (1)
     * Requires minimum score of 3 (language match) to auto-select.
     *
     * @return The best matching TrackOption, or null if no good match found
     */
    fun findBestAudioMatch(
        availableTracks: List<TrackOption>,
        preference: AudioTrackProperties
    ): TrackOption? {
        if (availableTracks.isEmpty()) return null

        val audioTracks = availableTracks.filter { it.type == TrackType.AUDIO }
        if (audioTracks.isEmpty()) return null

        val scoredTracks = audioTracks.map { track ->
            track to calculateAudioScore(track, preference)
        }

        val bestMatch = scoredTracks.maxByOrNull { it.second }

        // Require minimum score of 3 (language match) to auto-select
        return if (bestMatch != null && bestMatch.second >= 3) {
            bestMatch.first
        } else {
            null
        }
    }

    /**
     * Finds the best matching subtitle track based on stored preferences.
     * Scoring: language (3) + forced (2) + label (1)
     * Requires minimum score of 3 (language match) to auto-select.
     * Handles "Off" preference explicitly.
     *
     * @return The best matching TrackOption, or the "Off" option if preference.isOff is true, or null
     */
    fun findBestSubtitleMatch(
        availableTracks: List<TrackOption>,
        preference: SubtitleTrackProperties
    ): TrackOption? {
        if (availableTracks.isEmpty()) return null

        val subtitleTracks = availableTracks.filter { it.type == TrackType.TEXT }
        if (subtitleTracks.isEmpty()) return null

        // Handle "Off" preference
        if (preference.isOff) {
            return subtitleTracks.firstOrNull { it.isOff }
        }

        val scoredTracks = subtitleTracks
            .filter { !it.isOff }  // Exclude "Off" option when matching specific preferences
            .map { track ->
                track to calculateSubtitleScore(track, preference)
            }

        val bestMatch = scoredTracks.maxByOrNull { it.second }

        // Require minimum score of 3 (language match) to auto-select
        return if (bestMatch != null && bestMatch.second >= 3) {
            bestMatch.first
        } else {
            null
        }
    }

    private fun calculateAudioScore(
        track: TrackOption,
        preference: AudioTrackProperties
    ): Int {
        var score = 0

        // Language match: 3 points
        if (track.language != null && track.language == preference.language) {
            score += 3
        }

        // Channel count match: 2 points
        if (track.channelCount != null && track.channelCount == preference.channelCount) {
            score += 2
        }

        // Label match: 1 point
        if (track.label == preference.label) {
            score += 1
        }

        return score
    }

    private fun calculateSubtitleScore(
        track: TrackOption,
        preference: SubtitleTrackProperties
    ): Int {
        var score = 0

        // Language match: 3 points
        if (track.language != null && track.language == preference.language) {
            score += 3
        }

        // Forced flag match: 2 points
        if (track.forced == preference.forced) {
            score += 2
        }

        // Label match: 1 point
        if (track.label == preference.label) {
            score += 1
        }

        return score
    }
}
