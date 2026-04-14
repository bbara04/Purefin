package hu.bbara.purefin.tv

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import hu.bbara.purefin.feature.shared.home.AppViewModel
import hu.bbara.purefin.tv.home.TvHomeScreen
import hu.bbara.purefin.tv.home.ui.TvDrawerDestination
import hu.bbara.purefin.tv.home.ui.TvDrawerDestinationItem
import hu.bbara.purefin.tv.home.ui.TvNavigationDrawer
import hu.bbara.purefin.tv.library.ui.TvLibrariesOverviewScreen
import hu.bbara.purefin.core.model.LibraryKind

@Composable
fun TvAppScreen(
    viewModel: AppViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val libraries by viewModel.libraries.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val nextUp by viewModel.nextUp.collectAsState()
    val latestLibraryContent by viewModel.latestLibraryContent.collectAsState()

    var selectedDestination by rememberSaveable { androidx.compose.runtime.mutableStateOf(TvDrawerDestination.HOME) }

    val destinations = remember(libraries, selectedDestination) {
        listOf(
            TvDrawerDestinationItem(
                destination = TvDrawerDestination.HOME,
                label = "Home",
                icon = Icons.Outlined.Home,
                selected = selectedDestination == TvDrawerDestination.HOME
            ),
            TvDrawerDestinationItem(
                destination = TvDrawerDestination.LIBRARIES,
                label = "Libraries",
                icon = when {
                    libraries.any { it.type == LibraryKind.MOVIES } -> Icons.Outlined.Movie
                    libraries.any { it.type == LibraryKind.SERIES } -> Icons.Outlined.Tv
                    else -> Icons.Outlined.Collections
                },
                selected = selectedDestination == TvDrawerDestination.LIBRARIES
            )
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    TvNavigationDrawer(
        destinations = destinations,
        selectedDestination = selectedDestination,
        onDestinationSelected = { destination ->
            selectedDestination = destination
        },
        modifier = modifier.fillMaxSize()
    ) {
        when (selectedDestination) {
            TvDrawerDestination.HOME -> {
                TvHomeScreen(
                    libraries = libraries,
                    libraryContent = latestLibraryContent,
                    continueWatching = continueWatching,
                    nextUp = nextUp,
                    serverUrl = serverUrl,
                    onMovieSelected = viewModel::onMovieSelected,
                    onSeriesSelected = viewModel::onSeriesSelected,
                    onEpisodeSelected = viewModel::onEpisodeSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
            TvDrawerDestination.LIBRARIES -> {
                TvLibrariesOverviewScreen(
                    libraries = libraries,
                    onLibrarySelected = { library ->
                        viewModel.onLibrarySelected(library.id, library.name)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
