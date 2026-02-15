package hu.bbara.purefin.player.preference

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPreferencesRepository @Inject constructor(
    private val trackPreferencesDataStore: DataStore<TrackPreferences>
) {
    val preferences: Flow<TrackPreferences> = trackPreferencesDataStore.data

    fun getMediaPreferences(mediaId: String): Flow<MediaTrackPreferences?> {
        return preferences.map { it.mediaPreferences[mediaId] }
    }

    suspend fun saveAudioPreference(
        mediaId: String,
        properties: AudioTrackProperties
    ) {
        trackPreferencesDataStore.updateData { current ->
            val existingMediaPrefs = current.mediaPreferences[mediaId]
            val updatedMediaPrefs = existingMediaPrefs?.copy(audioPreference = properties)
                ?: MediaTrackPreferences(
                    mediaId = mediaId,
                    audioPreference = properties
                )

            current.copy(
                mediaPreferences = current.mediaPreferences + (mediaId to updatedMediaPrefs)
            )
        }
    }

    suspend fun saveSubtitlePreference(
        mediaId: String,
        properties: SubtitleTrackProperties
    ) {
        trackPreferencesDataStore.updateData { current ->
            val existingMediaPrefs = current.mediaPreferences[mediaId]
            val updatedMediaPrefs = existingMediaPrefs?.copy(subtitlePreference = properties)
                ?: MediaTrackPreferences(
                    mediaId = mediaId,
                    subtitlePreference = properties
                )

            current.copy(
                mediaPreferences = current.mediaPreferences + (mediaId to updatedMediaPrefs)
            )
        }
    }
}
