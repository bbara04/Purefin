package hu.bbara.purefin.tv.di

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.download.DownloadState
import hu.bbara.purefin.core.data.download.MediaDownloadController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.jellyfin.sdk.model.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TvPlaybackModule {

    @Provides
    @Singleton
    fun providePlaybackDataSourceFactory(
        okHttpDataSourceFactory: OkHttpDataSource.Factory
    ): DataSource.Factory {
        return okHttpDataSourceFactory
    }

    @Provides
    @Singleton
    fun provideMediaDownloadController(): MediaDownloadController {
        return TvNoOpMediaDownloadController
    }
}

internal object TvNoOpMediaDownloadController : MediaDownloadController {
    private val notDownloaded = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)

    override fun observeActiveDownloads(): Flow<Map<String, Float>> = flowOf(emptyMap())

    override fun observeDownloadState(contentId: String) = notDownloaded

    override suspend fun downloadMovie(movieId: UUID) = Unit

    override suspend fun cancelDownload(movieId: UUID) = Unit

    override suspend fun downloadEpisode(episodeId: UUID) = Unit

    override suspend fun downloadEpisodes(episodeIds: List<UUID>) = Unit

    override suspend fun cancelEpisodeDownload(episodeId: UUID) = Unit

    override suspend fun enableSmartDownload(seriesId: UUID) = Unit

    override suspend fun syncSmartDownloads() = Unit
}
