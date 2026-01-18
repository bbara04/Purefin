package hu.bbara.purefin.player.module

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object VideoPlayerModule {

    @OptIn(UnstableApi::class)
    @Provides
    @ViewModelScoped
    fun provideVideoPlayer(application: Application): Player {
        val trackSelector = DefaultTrackSelector(application)
        val audioAttributes =
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()

        trackSelector.setParameters(
            trackSelector
                .buildUponParameters()
                .setTunnelingEnabled(true)
//                .setPreferredAudioLanguage(
//                    appPreferences.getValue(appPreferences.preferredAudioLanguage)
//                )
//                .setPreferredTextLanguage(
//                    appPreferences.getValue(appPreferences.preferredSubtitleLanguage)
//                )
        )
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                25_000,
                55_000,
                5_000,
                5_000
            )
            .build()
        return ExoPlayer.Builder(application)
            .setPauseAtEndOfMediaItems(true)
            .setLoadControl(loadControl)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()
            .apply {
                playWhenReady = true
                pauseAtEndOfMediaItems = true
            }
    }
}
