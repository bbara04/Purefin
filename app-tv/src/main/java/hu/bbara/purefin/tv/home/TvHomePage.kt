package hu.bbara.purefin.tv.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import hu.bbara.purefin.feature.shared.home.AppViewModel
import hu.bbara.purefin.feature.shared.library.LibraryViewModel
import hu.bbara.purefin.tv.home.ui.TvHomeContent
import hu.bbara.purefin.tv.home.ui.TvHomeTabDestination
import hu.bbara.purefin.tv.home.ui.TvHomeTabItem
import hu.bbara.purefin.tv.home.ui.TvHomeTopBar
import hu.bbara.purefin.tv.library.ui.TvLibraryContent
import org.jellyfin.sdk.model.api.CollectionType

@Composable
fun TvHomePage(
    viewModel: AppViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    val libraries by viewModel.libraries.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val nextUp by viewModel.nextUp.collectAsState()
    val latestLibraryContent by viewModel.latestLibraryContent.collectAsState()
    val selectedLibraryItems by libraryViewModel.contents.collectAsState()

    val tabs = remember(libraries) {
        buildList {
            add(
                TvHomeTabItem(
                    destination = TvHomeTabDestination.SEARCH,
                    label = "Search",
                    icon = Icons.Outlined.Search,
                )
            )
            add(
                TvHomeTabItem(
                    destination = TvHomeTabDestination.HOME,
                    label = "Home",
                    icon = Icons.Outlined.Home,
                )
            )
            addAll(libraries.map {
                TvHomeTabItem(
                    destination = TvHomeTabDestination.LIBRARY,
                    label = it.name,
                    icon = when (it.type) {
                        CollectionType.MOVIES -> Icons.Outlined.Movie
                        CollectionType.TVSHOWS -> Icons.Outlined.Tv
                        else -> Icons.Outlined.Collections
                    },
                    libraryId = it.id
                )
            })
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    val safeSelectedTabIndex = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    val selectedTab = tabs.getOrNull(safeSelectedTabIndex)

    LaunchedEffect(selectedTab?.destination, selectedTab?.libraryId) {
        if (selectedTab?.destination == TvHomeTabDestination.LIBRARY) {
            val libraryId = selectedTab.libraryId ?: return@LaunchedEffect
            libraryViewModel.selectLibrary(libraryId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TvHomeTopBar(
                tabs = tabs,
                selectedTabIndex = safeSelectedTabIndex,
                onTabSelected = { index, _ ->
                    selectedTabIndex = index
                },
                isOfflineMode = isOfflineMode,
                onToggleOfflineMode = viewModel::toggleOfflineMode
            )
        }
    ) { innerPadding ->
        when (selectedTab?.destination) {
            TvHomeTabDestination.LIBRARY -> {
                TvLibraryContent(
                    libraryItems = selectedLibraryItems,
                    onMovieSelected = viewModel::onMovieSelected,
                    onSeriesSelected = viewModel::onSeriesSelected,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            TvHomeTabDestination.SEARCH,
            TvHomeTabDestination.HOME,
            null -> {
                TvHomeContent(
                    libraries = libraries,
                    libraryContent = latestLibraryContent,
                    continueWatching = continueWatching,
                    nextUp = nextUp,
                    onMovieSelected = viewModel::onMovieSelected,
                    onSeriesSelected = viewModel::onSeriesSelected,
                    onEpisodeSelected = viewModel::onEpisodeSelected,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
