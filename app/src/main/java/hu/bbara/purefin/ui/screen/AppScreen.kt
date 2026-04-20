package hu.bbara.purefin.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import hu.bbara.purefin.feature.browse.home.AppViewModel
import hu.bbara.purefin.ui.screen.download.DownloadsScreen
import hu.bbara.purefin.ui.screen.home.HomeScreen
import hu.bbara.purefin.ui.screen.home.components.HomeNavItem
import hu.bbara.purefin.ui.screen.libraries.LibrariesScreen

@Composable
fun AppScreen(
    viewModel: AppViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val libraries by viewModel.libraries.collectAsState()
    val libraryContent by viewModel.latestLibraryContent.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val nextUp by viewModel.nextUp.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val libraryNavItems = libraries.map {
        HomeNavItem(
            id = it.id,
            label = it.name,
            posterUrl = it.posterUrl
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose { }
    }

    when (selectedTab) {
        0 -> HomeScreen(
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
            onSettingsClick = {},
            onLogoutClick = viewModel::logout,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = modifier.fillMaxSize()
        )
        1 -> LibrariesScreen(
            items = libraryNavItems,
            onLibrarySelected = { item -> viewModel.onLibrarySelected(item.id, item.label) },
            onProfileClick = {},
            onSettingsClick = {},
            onLogoutClick = viewModel::logout,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = modifier.fillMaxSize()
        )
        2 -> DownloadsScreen(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = modifier.fillMaxSize()
        )
    }
}
