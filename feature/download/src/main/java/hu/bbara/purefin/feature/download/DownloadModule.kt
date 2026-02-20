package hu.bbara.purefin.feature.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    private const val DOWNLOAD_CHANNEL_ID = "purefin_downloads"

    @Provides
    @Singleton
    fun provideDownloadCache(@ApplicationContext context: Context): SimpleCache {
        val downloadDir = File(context.getExternalFilesDir(null), "downloads")
        return SimpleCache(downloadDir, NoOpCacheEvictor(), StandaloneDatabaseProvider(context))
    }

    @Provides
    @Singleton
    fun provideOkHttpDataSourceFactory(okHttpClient: OkHttpClient): OkHttpDataSource.Factory {
        return OkHttpDataSource.Factory(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideDownloadNotificationHelper(@ApplicationContext context: Context): DownloadNotificationHelper {
        return DownloadNotificationHelper(context, DOWNLOAD_CHANNEL_ID)
    }

    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        cache: SimpleCache,
        okHttpDataSourceFactory: OkHttpDataSource.Factory
    ): DownloadManager {
        return DownloadManager(
            context,
            StandaloneDatabaseProvider(context),
            cache,
            okHttpDataSourceFactory,
            Executors.newFixedThreadPool(2)
        )
    }

    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(
        cache: SimpleCache,
        okHttpDataSourceFactory: OkHttpDataSource.Factory
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(okHttpDataSourceFactory)
    }
}
