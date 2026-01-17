package hu.bbara.purefin

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.app.HomePage
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.client.JellyfinAuthInterceptor
import hu.bbara.purefin.login.ui.LoginScreen
import hu.bbara.purefin.session.UserSessionRepository
import hu.bbara.purefin.ui.theme.PurefinTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import javax.inject.Inject

@AndroidEntryPoint
class PurefinActivity : ComponentActivity() {

    @Inject
    lateinit var userSessionRepository: UserSessionRepository

    @Inject
    lateinit var jellyfinApiClient: JellyfinApiClient

    @Inject
    lateinit var authInterceptor: JellyfinAuthInterceptor

    private val imageOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor(authInterceptor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch { init() }
        configureImageLoader()
        enableEdgeToEdge()
        setContent {
            PurefinTheme {
                val isLoggedIn by userSessionRepository.isLoggedIn.collectAsState(initial = false)
                if (isLoggedIn) {
                    HomePage()
                } else {
                    LoginScreen()
                }
            }
        }
    }

    private suspend fun init() {
        jellyfinApiClient.updateApiClient()
    }

    private fun configureImageLoader() {
        SingletonImageLoader.setSafe { context ->
            val builder = ImageLoader.Builder(context)
                .components {
                    add(
                        OkHttpNetworkFetcherFactory(
                            callFactory = { imageOkHttpClient }
                        )
                    )
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache").absolutePath.toPath())
                        .maxSizeBytes(30000000)
                        .build()
                }
                .crossfade(true)

            if (isDebuggable()) {
                builder.logger(DebugLogger())
            }

            builder.build()
        }
    }

    private fun isDebuggable(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
