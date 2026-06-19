package hu.bbara.purefin.data.jellyfin.etag

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.jellyfin.JellyfinSdkClient
import okhttp3.OkHttpClient
import org.jellyfin.sdk.api.okhttp.OkHttpFactory
import javax.inject.Singleton

/**
 * Provides the [OkHttpClient] (with [ETagInterceptor] installed) and the
 * [OkHttpFactory] used by the Jellyfin SDK. The client is qualified with
 * [JellyfinSdkClient] so it is distinct from the unqualified OkHttpClient
 * in `core.jellyfin.JellyfinNetworkModule`, which is used for image and
 * media streaming. The Jellyfin SDK provides its own auth header on every
 * request, so the SDK-bound client does not need `JellyfinAuthInterceptor`.
 */
@Module
@InstallIn(SingletonComponent::class)
object JellyfinOkHttpModule {

    @Provides
    @Singleton
    @JellyfinSdkClient
    fun provideJellyfinSdkOkHttpClient(etagInterceptor: ETagInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(etagInterceptor)
            .build()

    @Provides
    @Singleton
    @JellyfinSdkClient
    fun provideJellyfinOkHttpFactory(@JellyfinSdkClient client: OkHttpClient): OkHttpFactory =
        OkHttpFactory(base = client)
}
