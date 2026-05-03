package hu.bbara.purefin.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import hu.bbara.purefin.feature.browse.home.AppViewModel
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.update.AppUpdateInstaller
import hu.bbara.purefin.ui.screen.download.DownloadsScreen
import hu.bbara.purefin.ui.screen.home.HomeScreen
import hu.bbara.purefin.ui.screen.libraries.LibrariesScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun AppScreen(
    viewModel: AppViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val libraries by viewModel.libraries.collectAsState()
    val libraryContent by viewModel.latestLibraryContent.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val nextUp by viewModel.nextUp.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val navigationManager = LocalNavigationManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appUpdateInstaller = remember(context) { AppUpdateInstaller(context) }
    var isCheckingForUpdates by remember { mutableStateOf(false) }

    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(AppTabRoute.Home) as NavBackStack<AppTabRoute>
    val currentRoute = backStack.lastOrNull() ?: AppTabRoute.Home
    val selectedTab = currentRoute.toTabIndex()

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    val onTabSelected = remember(backStack) {
        { selectedIndex: Int ->
            val route = selectedIndex.toAppTabRoute()
            if (backStack.lastOrNull() != route) {
                backStack.add(route)
            }
        }
    }
    val onCheckForUpdates = {
        if (!isCheckingForUpdates) {
            coroutineScope.launch {
                isCheckingForUpdates = true
                val message = try {
                    appUpdateInstaller.checkForUpdateAndInstall()
                } catch (e: Exception) {
                    e.message ?: "Update check failed"
                } finally {
                    isCheckingForUpdates = false
                }
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val tabEntryProvider = entryProvider {
        entry<AppTabRoute.Home>(metadata = appTabMetadata(AppTabRoute.Home)) {
            HomeScreen(
                libraries = libraries,
                libraryContent = libraryContent,
                suggestions = suggestions,
                continueWatching = continueWatching,
                nextUp = nextUp,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::onRefresh,
                onMediaSelected = viewModel::onMediaSelected,
                onLibrarySelected = { library ->
                    viewModel.onLibrarySelected(
                        library.id,
                        library.name
                    )
                },
                onProfileClick = {},
                onCheckForUpdates = onCheckForUpdates,
                isCheckingForUpdates = isCheckingForUpdates,
                onSettingsClick = {},
                onLogoutClick = viewModel::logout,
                onSearchClick = viewModel::openSearch,
                snackbarHostState = snackbarHostState,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
        entry<AppTabRoute.Libraries>(metadata = appTabMetadata(AppTabRoute.Libraries)) {
            LibrariesScreen(
                items = libraries,
                onLibrarySelected = { item -> viewModel.onLibrarySelected(item.id, item.name) },
                onSearchClick = viewModel::openSearch,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
        entry<AppTabRoute.Downloads>(metadata = appTabMetadata(AppTabRoute.Downloads)) {
            DownloadsScreen(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                navigationManager.pop()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
        popTransitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
        predictivePopTransitionSpec = { _ -> fadeIn(tween(220)) togetherWith fadeOut(tween(220)) },
        entryProvider = tabEntryProvider,
        modifier = modifier.fillMaxSize()
    )
}

@Serializable
private sealed interface AppTabRoute : NavKey {
    @Serializable
    data object Home : AppTabRoute

    @Serializable
    data object Libraries : AppTabRoute

    @Serializable
    data object Downloads : AppTabRoute
}

private fun AppTabRoute.toTabIndex(): Int = when (this) {
    AppTabRoute.Home -> 0
    AppTabRoute.Libraries -> 1
    AppTabRoute.Downloads -> 2
}

private fun Int.toAppTabRoute(): AppTabRoute = when (this) {
    0 -> AppTabRoute.Home
    1 -> AppTabRoute.Libraries
    2 -> AppTabRoute.Downloads
    else -> AppTabRoute.Home
}

private fun appTabMetadata(route: AppTabRoute): Map<String, Any> =
    mapOf(APP_TAB_INDEX_METADATA to route.toTabIndex())

private fun Map<String, Any>.appTabIndex(): Int =
    this[APP_TAB_INDEX_METADATA] as? Int ?: 0

private const val APP_TAB_INDEX_METADATA = "app_tab_index"
