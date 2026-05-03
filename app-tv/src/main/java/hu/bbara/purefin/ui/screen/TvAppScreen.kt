package hu.bbara.purefin.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import hu.bbara.purefin.feature.browse.home.AppViewModel
import hu.bbara.purefin.model.LibraryKind
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.ui.screen.home.TvHomeScreen
import hu.bbara.purefin.ui.screen.home.components.TvDrawerDestinationItem
import hu.bbara.purefin.ui.screen.home.components.TvNavigationDrawer
import hu.bbara.purefin.ui.screen.library.TvLibraryScreen
import hu.bbara.purefin.ui.screen.settings.TvSettingsScreen

@Composable
fun TvAppScreen(
    viewModel: AppViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val libraries by viewModel.libraries.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val nextUp by viewModel.nextUp.collectAsState()
    val latestLibraryContent by viewModel.latestLibraryContent.collectAsState()
    val navigationManager = LocalNavigationManager.current

    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(Route.Home) as NavBackStack<Route>
    val selectedDestination = backStack.lastOrNull() ?: Route.Home

    val destinations = remember(libraries) {
        listOf(
            TvDrawerDestinationItem(
                destination = Route.Home,
                label = "Home",
                icon = Icons.Outlined.Home
            )
        ) + libraries.map { library ->
            val destination = Route.LibraryRoute(
                library = LibraryDto(id = library.id, name = library.name)
            )
            TvDrawerDestinationItem(
                destination = destination,
                label = library.name,
                icon = when (library.type) {
                    LibraryKind.MOVIES -> Icons.Outlined.Movie
                    LibraryKind.SERIES -> Icons.Outlined.Tv
                }
            )
        } + TvDrawerDestinationItem(
            destination = Route.SettingsRoute,
            label = "Settings",
            icon = Icons.Outlined.Settings
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    val tvEntryProvider = entryProvider {
        entry<Route.Home> {
            TvHomeScreen(
                libraries = libraries,
                libraryContent = latestLibraryContent,
                continueWatching = continueWatching,
                nextUp = nextUp,
                onMediaSelected = viewModel::onMediaSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
        entry<Route.LibraryRoute> { route ->
            TvLibraryScreen(
                library = route.library,
                onMediaSelected = viewModel::onMediaSelected,
                modifier = Modifier.fillMaxSize()
            )
        }
        entry<Route.SettingsRoute> {
            TvSettingsScreen(
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    TvNavigationDrawer(
        destinations = destinations,
        selectedDestination = selectedDestination,
        onDestinationSelected = { destination ->
            if (selectedDestination != destination) {
                backStack.clear()
                backStack.add(Route.Home)
                if (destination != Route.Home) {
                    backStack.add(destination)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
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
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = tvEntryProvider,
            modifier = Modifier.fillMaxSize()
        )
    }
}
