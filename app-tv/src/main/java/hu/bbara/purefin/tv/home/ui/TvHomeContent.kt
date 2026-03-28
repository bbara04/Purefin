package hu.bbara.purefin.tv.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
    val visibleLibraries = remember(libraries, libraryContent) {
        libraries.filter { libraryContent[it.id]?.isEmpty() != true }
    }
    val continueWatchingFocusRequester = remember { FocusRequester() }
    val nextUpFocusRequester = remember { FocusRequester() }
    val libraryFocusRequesters = remember(visibleLibraries.map { it.id }) {
        visibleLibraries.associate { it.id to FocusRequester() }
    }
    val firstSectionFocusRequester = when {
        continueWatching.isNotEmpty() -> continueWatchingFocusRequester
        nextUp.isNotEmpty() -> nextUpFocusRequester
        else -> visibleLibraries.firstOrNull()?.let { libraryFocusRequesters[it.id] }
    }

    LaunchedEffect(firstSectionFocusRequester) {
        firstSectionFocusRequester?.requestFocus()
    }

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
                firstItemFocusRequester = continueWatchingFocusRequester,
                downFocusRequester = when {
                    nextUp.isNotEmpty() -> nextUpFocusRequester
                    else -> visibleLibraries.firstOrNull()?.let { libraryFocusRequesters[it.id] }
                },
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
                firstItemFocusRequester = nextUpFocusRequester,
                upFocusRequester = continueWatching.takeIf { it.isNotEmpty() }?.let { continueWatchingFocusRequester },
                downFocusRequester = visibleLibraries.firstOrNull()?.let { libraryFocusRequesters[it.id] },
                onEpisodeSelected = onEpisodeSelected
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(
            items = visibleLibraries,
            key = { it.id }
        ) { item ->
            val libraryIndex = visibleLibraries.indexOfFirst { it.id == item.id }
            val previousLibraryFocusRequester = visibleLibraries
                .getOrNull(libraryIndex - 1)
                ?.let { libraryFocusRequesters[it.id] }
            val nextLibraryFocusRequester = visibleLibraries
                .getOrNull(libraryIndex + 1)
                ?.let { libraryFocusRequesters[it.id] }

            TvLibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                firstItemFocusRequester = libraryFocusRequesters[item.id],
                upFocusRequester = if (libraryIndex == 0) {
                    when {
                        nextUp.isNotEmpty() -> nextUpFocusRequester
                        continueWatching.isNotEmpty() -> continueWatchingFocusRequester
                        else -> null
                    }
                } else {
                    previousLibraryFocusRequester
                },
                downFocusRequester = nextLibraryFocusRequester,
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
