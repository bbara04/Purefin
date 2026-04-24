package hu.bbara.purefin.player.module

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Module
@InstallIn(SingletonComponent::class)
object PlaybackNetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpDataSourceFactory(okHttpClient: OkHttpClient): OkHttpDataSource.Factory {
        return OkHttpDataSource.Factory(okHttpClient)
    }
}
