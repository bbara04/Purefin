package hu.bbara.purefin.tv.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.client.PlaybackProfileFamily

@Module
@InstallIn(SingletonComponent::class)
object TvPlaybackProfileFamilyModule {

    @Provides
    fun providePlaybackProfileFamily(): PlaybackProfileFamily = PlaybackProfileFamily.TV
}
