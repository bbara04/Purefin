package hu.bbara.purefin.data.jellyfin

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.jellyfin.client.JellyfinAuthInterceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JellyfinNetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: JellyfinAuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }
}
