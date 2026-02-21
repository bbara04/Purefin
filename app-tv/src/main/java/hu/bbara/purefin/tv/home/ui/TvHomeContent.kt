package hu.bbara.purefin.tv.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

@Composable
fun TvHomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            TvContinueWatchingSection(
                items = continueWatching,
                onMovieSelected = onMovieSelected,
                onEpisodeSelected = onEpisodeSelected
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            TvNextUpSection(
                items = nextUp,
                onEpisodeSelected = onEpisodeSelected
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(
            items = libraries.filter { libraryContent[it.id]?.isEmpty() != true },
            key = { it.id }
        ) { item ->
            TvLibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
