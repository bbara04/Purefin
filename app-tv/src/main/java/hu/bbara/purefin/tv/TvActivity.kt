package hu.bbara.purefin.tv

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import dagger.hilt.android.AndroidEntryPoint
import hu.bbara.purefin.data.SessionBootstrapper
import hu.bbara.purefin.data.UserSessionRepository
import hu.bbara.purefin.feature.update.AppUpdateInfo
import hu.bbara.purefin.feature.update.AppUpdateViewModel
import hu.bbara.purefin.jellyfin.JellyfinAuthInterceptor
import hu.bbara.purefin.navigation.NavigationCommand
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.navigation.LocalNavigationBackStack
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.ui.screen.login.LoginScreen
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import hu.bbara.purefin.ui.theme.AppTheme
import hu.bbara.purefin.ui.theme.backgroundDark
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import javax.inject.Inject

@AndroidEntryPoint
class TvActivity : ComponentActivity() {

    @Inject
    lateinit var entryBuilders: Set<@JvmSuppressWildcards EntryProviderScope<Route>.() -> Unit>

    @Inject
    lateinit var userSessionRepository: UserSessionRepository

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var sessionBootstrapper: SessionBootstrapper

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
            AppTheme {
                MainApp(
                    userSessionRepository = userSessionRepository,
                    entryBuilders = entryBuilders,
                    navigationManager = navigationManager
                )
            }
        }
    }

    private suspend fun init() {
        sessionBootstrapper.initialize()
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
                        .maxSizePercent(context, 0.08)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("image_cache").absolutePath.toPath())
                        .maxSizeBytes(30_000_000)
                        .build()
                }
                .crossfade(true)

            if (isDebuggable()) {
                builder.logger(DebugLogger())
            }

            builder.build()
        }
    }

    private fun isDebuggable(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    @Composable
    fun MainApp(
        userSessionRepository: UserSessionRepository,
        entryBuilders: Set<@JvmSuppressWildcards EntryProviderScope<Route>.() -> Unit>,
        navigationManager: NavigationManager,
        updateViewModel: AppUpdateViewModel = hiltViewModel()
    ) {
        var sessionLoaded by remember { mutableStateOf(false) }
        val isLoggedIn by userSessionRepository.isLoggedIn.collectAsState(initial = false)
        val availableUpdate by updateViewModel.availableUpdate.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            userSessionRepository.isLoggedIn.collect {
                sessionLoaded = true
            }
        }

        LaunchedEffect(updateViewModel) {
            updateViewModel.checkForUpdates(showUpToDateMessage = false)
        }

        LaunchedEffect(updateViewModel, context) {
            updateViewModel.snackbarMessages.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

        if (!sessionLoaded) {
            PurefinWaitingScreen(modifier = Modifier.fillMaxSize())
        } else if (isLoggedIn) {
            @Suppress("UNCHECKED_CAST")
            val backStack = rememberNavBackStack(Route.Home) as NavBackStack<Route>
            val appEntryProvider = entryProvider {
                entryBuilders.forEach { builder -> builder() }
            }

            LaunchedEffect(navigationManager, backStack) {
                navigationManager.commands.collect { command ->
                    when (command) {
                        NavigationCommand.Pop -> if (backStack.size > 1) backStack.removeLastOrNull()
                        is NavigationCommand.Navigate -> backStack.add(command.route)
                        is NavigationCommand.ReplaceAll -> {
                            backStack.clear()
                            backStack.add(command.route)
                        }
                    }
                }
            }

            CompositionLocalProvider(
                LocalNavigationManager provides navigationManager,
                LocalNavigationBackStack provides backStack.toList()
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { navigationManager.pop() },
                    modifier = Modifier.fillMaxSize().background(backgroundDark),
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 140
                            )
                        ) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 140))
                    },
                    popTransitionSpec = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 140
                            )
                        ) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 140))
                    },
                    predictivePopTransitionSpec = { _ ->
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 140
                            )
                        ) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 140))
                    },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = appEntryProvider
                )
            }
        } else {
            LoginScreen()
        }

        availableUpdate?.let { update ->
            TvUpdateAvailableDialog(
                update = update,
                onAccept = updateViewModel::acceptUpdate,
                onDecline = updateViewModel::declineUpdate
            )
        }
    }

    @Composable
    private fun TvUpdateAvailableDialog(
        update: AppUpdateInfo,
        onAccept: () -> Unit,
        onDecline: () -> Unit
    ) {
        val versionLabel = update.versionName?.takeIf { it.isNotBlank() } ?: update.versionCode.toString()
        val releaseNotes = update.releaseNotes?.takeIf { it.isNotBlank() }
        val updateText = if (releaseNotes == null) {
            "Purefin TV $versionLabel is available.\n\nVersion code: ${update.versionCode}"
        } else {
            "Purefin TV $versionLabel is available.\n\nVersion code: ${update.versionCode}\n\n$releaseNotes"
        }

        AlertDialog(
            onDismissRequest = onDecline,
            title = { Text("Update available") },
            text = { Text(updateText) },
            confirmButton = {
                TextButton(onClick = onAccept) {
                    Text("Install")
                }
            },
            dismissButton = {
                TextButton(onClick = onDecline) {
                    Text("Not now")
                }
            }
        )
    }
}
