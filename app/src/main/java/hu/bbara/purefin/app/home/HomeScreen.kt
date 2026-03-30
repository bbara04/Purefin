package hu.bbara.purefin.app.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import hu.bbara.purefin.app.home.ui.HomeContent
import hu.bbara.purefin.app.home.ui.HomeDiscoveryTopBar
import hu.bbara.purefin.app.home.ui.HomeSearchOverlay
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    onLibrarySelected: (LibraryItem) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    val subtitle = remember(continueWatching, nextUp, libraries) {
        when {
            continueWatching.isNotEmpty() -> "Continue where you left off"
            nextUp.isNotEmpty() -> "Fresh episodes waiting for you"
            libraries.isNotEmpty() -> "Browse your latest additions"
            else -> "Pull to refresh or explore your libraries"
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            HomeDiscoveryTopBar(
                title = "Watch now",
                subtitle = subtitle,
                scrollBehavior = scrollBehavior,
                onSearchClick = { isSearchVisible = true },
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
        Box(modifier = Modifier.fillMaxSize()) {
            HomeContent(
                libraries = libraries,
                libraryContent = libraryContent,
                continueWatching = continueWatching,
                nextUp = nextUp,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected,
                onLibrarySelected = onLibrarySelected,
                onBrowseLibrariesClick = { onTabSelected(1) },
                modifier = Modifier.padding(innerPadding)
            )
            HomeSearchOverlay(
                visible = isSearchVisible,
                topPadding = innerPadding.calculateTopPadding(),
                onDismiss = { isSearchVisible = false },
                onMovieSelected = {
                    isSearchVisible = false
                    onMovieSelected(it)
                },
                onSeriesSelected = {
                    isSearchVisible = false
                    onSeriesSelected(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}
