@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.feature.browse.home.ContinueWatchingItem
import hu.bbara.purefin.feature.browse.home.FocusableItem
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.feature.browse.home.NextUpItem
import hu.bbara.purefin.feature.browse.home.PosterItem
import java.util.UUID

internal const val TvHomeInitialFocusTag = "tv-home-initial-focus-item"
internal const val TvHomeContentViewportTag = "tv-home-content-viewport"

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
    val hasContinueWatching = continueWatching.isNotEmpty()
    val hasNextUp = nextUp.isNotEmpty()
    val hasLibraries = visibleLibraries.isNotEmpty()
    val hasVisibleContent = hasContinueWatching || hasNextUp || hasLibraries
    val firstVisibleLibraryId = visibleLibraries.firstOrNull()?.id
    val initialFocusRequester = remember { FocusRequester() }
    val firstAvailableItemKey = itemRegistry.firstAvailableItemId
    var initialFocusApplied by remember { mutableStateOf(false) }
    val topOffsetPx = with(LocalDensity.current) { TvHomeFocusedItemTopOffset.toPx() }
    val columnBringIntoViewSpec = remember(topOffsetPx) {
        tvHomeColumnBringIntoViewSpec(topOffsetPx = topOffsetPx)
    }

    LaunchedEffect(firstAvailableItemKey, initialFocusApplied) {
        if (!initialFocusApplied && firstAvailableItemKey != null) {
            withFrameNanos { }
            initialFocusRequester.requestFocus()
            initialFocusApplied = true
        }
    }

    CompositionLocalProvider(LocalBringIntoViewSpec provides columnBringIntoViewSpec) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(scheme.background)
                .testTag(TvHomeContentViewportTag),
            contentPadding = contentPadding
        ) {
            item(key = "tv-home-top-spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (hasContinueWatching) {
                item(key = "tv-home-continue-watching") {
                    TvContinueWatchingSection(
                        items = continueWatching,
                        onFocusedItem = onMediaFocused,
                        onMovieSelected = onMovieSelected,
                        onEpisodeSelected = onEpisodeSelected,
                        firstItemFocusRequester = initialFocusRequester,
                        firstItemTestTag = TvHomeInitialFocusTag,
                        rowTestTag = TvHomeContinueWatchingRowTag
                    )
                }
            }

            if (hasContinueWatching && (hasNextUp || hasLibraries)) {
                item(key = "tv-home-post-continue-gap") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (hasNextUp) {
                item(key = "tv-home-next-up") {
                    TvNextUpSection(
                        items = nextUp,
                        onFocusedItem = onMediaFocused,
                        onEpisodeSelected = onEpisodeSelected,
                        firstItemFocusRequester = initialFocusRequester.takeIf { !hasContinueWatching },
                        firstItemTestTag = TvHomeInitialFocusTag.takeIf { !hasContinueWatching },
                        rowTestTag = TvHomeNextUpRowTag
                    )
                }
            }

            if (hasLibraries && (hasContinueWatching || hasNextUp)) {
                item(key = "tv-home-post-next-up-gap") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            itemsIndexed(
                items = visibleLibraries,
                key = { _, library -> library.id }
            ) { index, library ->
                TvLibraryPosterSection(
                    title = library.name,
                    items = libraryContent[library.id].orEmpty(),
                    onFocusedItem = onMediaFocused,
                    firstItemFocusRequester = initialFocusRequester.takeIf {
                        !hasContinueWatching &&
                            !hasNextUp &&
                            library.id == firstVisibleLibraryId
                    },
                    firstItemTestTag = TvHomeInitialFocusTag.takeIf {
                        !hasContinueWatching &&
                            !hasNextUp &&
                            library.id == firstVisibleLibraryId
                    },
                    rowTestTag = tvHomeLibraryRowTag(library.id),
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    onEpisodeSelected = onEpisodeSelected
                )

                if (index < visibleLibraries.lastIndex) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (hasVisibleContent) {
                item(key = "tv-home-trailing-space") {
                    Spacer(modifier = Modifier.height(TvHomeBringIntoViewTrailingSpace))
                }
            }
        }
    }
}
