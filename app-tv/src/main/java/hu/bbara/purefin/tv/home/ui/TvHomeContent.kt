package hu.bbara.purefin.tv.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.FocusableItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID

internal const val TvHomeInitialFocusTag = "tv-home-initial-focus-item"

@Composable
fun TvHomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    onMediaFocused: (FocusableItem) -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val itemRegistry = rememberTvHomeItemRegistry(
        libraries = libraries,
        libraryContent = libraryContent,
        continueWatching = continueWatching,
        nextUp = nextUp
    )
    val visibleLibraries = itemRegistry.visibleLibraries
    val (nextUpRef, continueWatchingRef) = remember { FocusRequester.createRefs() }
    val libraryRefs = remember(visibleLibraries) {
        visibleLibraries.associate { it.id to FocusRequester() }
    }
    val initialFocusRequester = remember { FocusRequester() }
    val firstVisibleLibraryId = visibleLibraries.firstOrNull()?.id
    val firstAvailableItemKey = itemRegistry.firstAvailableItemId
    var initialFocusApplied by remember { mutableStateOf(false) }

    LaunchedEffect(firstAvailableItemKey, initialFocusApplied) {
        if (!initialFocusApplied && firstAvailableItemKey != null) {
            withFrameNanos { }
            initialFocusRequester.requestFocus()
            initialFocusApplied = true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = contentPadding
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            TvContinueWatchingSection(
                items = continueWatching,
                onFocusedItem = onMediaFocused,
                onMovieSelected = onMovieSelected,
                onEpisodeSelected = onEpisodeSelected,
                firstItemFocusRequester = initialFocusRequester.takeIf { continueWatching.isNotEmpty() },
                firstItemTestTag = TvHomeInitialFocusTag.takeIf { continueWatching.isNotEmpty() },
                modifier = Modifier.focusRequester(continueWatchingRef)
                    .focusProperties {
                        down = nextUpRef
                    }
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        item {
            TvNextUpSection(
                items = nextUp,
                onFocusedItem = onMediaFocused,
                onEpisodeSelected = onEpisodeSelected,
                firstItemFocusRequester = initialFocusRequester
                    .takeIf { continueWatching.isEmpty() && nextUp.isNotEmpty() },
                firstItemTestTag = TvHomeInitialFocusTag
                    .takeIf { continueWatching.isEmpty() && nextUp.isNotEmpty() },
                modifier = Modifier.focusRequester(nextUpRef)
                    .focusProperties {
                        up = continueWatchingRef
                        libraryRefs.values.firstOrNull()?.let { down = it }
                    }
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        items(
            items = visibleLibraries,
            key = { it.id }
        ) { item ->
            val ref = libraryRefs[item.id]
            TvLibraryPosterSection(
                title = item.name,
                items = libraryContent[item.id] ?: emptyList(),
                action = "See All",
                onFocusedItem = onMediaFocused,
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected,
                firstItemFocusRequester = initialFocusRequester.takeIf {
                    continueWatching.isEmpty() &&
                        nextUp.isEmpty() &&
                        item.id == firstVisibleLibraryId
                },
                firstItemTestTag = TvHomeInitialFocusTag.takeIf {
                    continueWatching.isEmpty() &&
                        nextUp.isEmpty() &&
                        item.id == firstVisibleLibraryId
                },
                modifier = (if (ref != null) Modifier.focusRequester(ref) else Modifier)
                    .focusProperties {
                        up = nextUpRef
                        libraryRefs.values.dropWhile { it != ref }.drop(1).firstOrNull()
                            ?.let { down = it }
                    }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
