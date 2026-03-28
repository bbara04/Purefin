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
    val continueWatchingSectionFocusRequester = remember { FocusRequester() }
    val continueWatchingFirstItemFocusRequester = remember { FocusRequester() }
    val nextUpSectionFocusRequester = remember { FocusRequester() }
    val nextUpFirstItemFocusRequester = remember { FocusRequester() }
    val librarySectionFocusRequesters = remember(visibleLibraries.map { it.id }) {
        visibleLibraries.associate { library -> library.id to FocusRequester() }
    }
    val libraryFirstItemFocusRequesters = remember(visibleLibraries.map { it.id }) {
        visibleLibraries.associate { library -> library.id to FocusRequester() }
    }
    val firstSectionFocusRequester = when {
        continueWatching.isNotEmpty() -> continueWatchingSectionFocusRequester
        nextUp.isNotEmpty() -> nextUpSectionFocusRequester
        else -> visibleLibraries.firstOrNull()?.let { librarySectionFocusRequesters[it.id] }
    }
    val firstVisibleLibrarySectionFocusRequester = visibleLibraries.firstOrNull()
        ?.let { librarySectionFocusRequesters[it.id] }
    val firstHomeRowBelowContinueWatching = when {
        nextUp.isNotEmpty() -> nextUpSectionFocusRequester
        else -> firstVisibleLibrarySectionFocusRequester
    }
    val firstHomeRowAboveLibraries = when {
        nextUp.isNotEmpty() -> nextUpSectionFocusRequester
        continueWatching.isNotEmpty() -> continueWatchingSectionFocusRequester
        else -> null
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
                sectionFocusRequester = continueWatchingSectionFocusRequester,
                firstItemFocusRequester = continueWatchingFirstItemFocusRequester,
                downFocusRequester = firstHomeRowBelowContinueWatching,
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
                sectionFocusRequester = nextUpSectionFocusRequester,
                firstItemFocusRequester = nextUpFirstItemFocusRequester,
                upFocusRequester = continueWatching.takeIf { it.isNotEmpty() }
                    ?.let { continueWatchingSectionFocusRequester },
                downFocusRequester = firstVisibleLibrarySectionFocusRequester,
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
            val previousLibrarySectionFocusRequester = visibleLibraries
                .getOrNull(libraryIndex - 1)
                ?.let { librarySectionFocusRequesters[it.id] }
            val nextLibrarySectionFocusRequester = visibleLibraries
                .getOrNull(libraryIndex + 1)
                ?.let { librarySectionFocusRequesters[it.id] }

            TvLibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                sectionFocusRequester = librarySectionFocusRequesters.getValue(item.id),
                firstItemFocusRequester = libraryFirstItemFocusRequesters.getValue(item.id),
                upFocusRequester = if (libraryIndex == 0) {
                    firstHomeRowAboveLibraries
                } else {
                    previousLibrarySectionFocusRequester
                },
                downFocusRequester = nextLibrarySectionFocusRequester,
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
