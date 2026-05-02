package hu.bbara.purefin.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.screen.AppBottomBar
import hu.bbara.purefin.ui.screen.home.components.HomeContent
import hu.bbara.purefin.ui.screen.home.components.HomeTopBar
import java.util.UUID

@Composable
fun HomeScreen(
    libraries: List<LibraryUiModel>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    suggestions: List<MediaUiModel>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMediaSelected: (MediaUiModel) -> Unit,
    onLibrarySelected: (LibraryUiModel) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick,
                onLogoutClick = onLogoutClick
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        HomeContent(
            libraries = libraries,
            libraryContent = libraryContent,
            suggestions = suggestions,
            continueWatching = continueWatching,
            nextUp = nextUp,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            onMediaSelected = onMediaSelected,
            onLibrarySelected = onLibrarySelected,
            onBrowseLibrariesClick = { onTabSelected(1) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}
