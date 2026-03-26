package hu.bbara.purefin.app.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.app.home.ui.HomeContent
import hu.bbara.purefin.app.home.ui.HomeTopBar
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

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
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            HomeTopBar()
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
            continueWatching = continueWatching,
            nextUp = nextUp,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            onMovieSelected = onMovieSelected,
            onSeriesSelected = onSeriesSelected,
            onEpisodeSelected = onEpisodeSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
