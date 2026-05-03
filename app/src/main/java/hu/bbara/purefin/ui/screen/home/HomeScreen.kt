package hu.bbara.purefin.ui.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
    onMarkWatched: (MediaUiModel, Boolean) -> Unit,
    onProfileClick: () -> Unit,
    onCheckForUpdates: () -> Unit,
    isCheckingForUpdates: Boolean,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
                onCheckForUpdates = onCheckForUpdates,
                isCheckingForUpdates = isCheckingForUpdates,
                onSettingsClick = onSettingsClick,
                onLogoutClick = onLogoutClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            onMarkAsWatched = onMarkWatched,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
