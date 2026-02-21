package hu.bbara.purefin.app.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import hu.bbara.purefin.app.home.ui.DownloadsContent
import hu.bbara.purefin.app.home.ui.HomeContent
import hu.bbara.purefin.app.home.ui.HomeNavItem
import hu.bbara.purefin.app.home.ui.HomeTopBar
import hu.bbara.purefin.app.home.ui.LibrariesContent
import hu.bbara.purefin.feature.shared.home.HomePageViewModel
import org.jellyfin.sdk.model.api.CollectionType

@Composable
fun HomePage(
    viewModel: HomePageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val libraries = viewModel.libraries.collectAsState().value
    val isOfflineMode = viewModel.isOfflineMode.collectAsState().value
    val libraryNavItems = libraries.map {
        HomeNavItem(
            id = it.id,
            label = it.name,
            icon = when (it.type) {
                CollectionType.MOVIES -> Icons.Outlined.Movie
                CollectionType.TVSHOWS -> Icons.Outlined.Tv
                else -> Icons.Outlined.Collections
            },
        )
    }
    val continueWatching = viewModel.continueWatching.collectAsState()
    val nextUp = viewModel.nextUp.collectAsState()
    val latestLibraryContent = viewModel.latestLibraryContent.collectAsState()

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            HomeTopBar(
                isOfflineMode = isOfflineMode,
                onToggleOfflineMode = viewModel::toggleOfflineMode
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.Collections, contentDescription = "Libraries") },
                    label = { Text("Libraries") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.Download, contentDescription = "Downloads") },
                    label = { Text("Downloads") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeContent(
                libraries = libraries,
                libraryContent = latestLibraryContent.value,
                continueWatching = continueWatching.value,
                nextUp = nextUp.value,
                onMovieSelected = viewModel::onMovieSelected,
                onSeriesSelected = viewModel::onSeriesSelected,
                onEpisodeSelected = viewModel::onEpisodeSelected,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> LibrariesContent(
                items = libraryNavItems,
                onLibrarySelected = { item -> viewModel.onLibrarySelected(item.id, item.label) },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> DownloadsContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
