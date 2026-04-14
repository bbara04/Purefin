package hu.bbara.purefin.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.client.PlaybackProfileFamily

@Module
@InstallIn(SingletonComponent::class)
object PlaybackProfileFamilyModule {

    @Provides
    fun providePlaybackProfileFamily(): PlaybackProfileFamily = PlaybackProfileFamily.MOBILE
}