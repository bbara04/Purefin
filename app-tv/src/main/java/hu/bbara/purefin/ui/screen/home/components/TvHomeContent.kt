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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.feature.browse.home.LibraryItem
import java.util.UUID

internal const val TvHomeInitialFocusTag = "tv-home-initial-focus-item"
internal const val TvHomeContentViewportTag = "tv-home-content-viewport"

@Composable
fun TvHomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
    onMediaFocused: (MediaUiModel) -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val topOffsetPx = with(LocalDensity.current) { TvHomeFocusedItemTopOffset.toPx() }
    val hasContinueWatching = continueWatching.isNotEmpty()
    val hasNextUp = nextUp.isNotEmpty()
    val hasVisibleContent = hasContinueWatching || hasNextUp
    val initialFocusRequester = remember { FocusRequester() }

    CompositionLocalProvider(
        LocalBringIntoViewSpec provides tvHomeColumnBringIntoViewSpec(topOffsetPx)
    ) {
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

            itemsIndexed(
                items = libraries,
                key = { _, library -> library.id }
            ) { _, library ->
                TvLibraryPosterSection(
                    title = library.name,
                    items = libraryContent[library.id].orEmpty(),
                    onFocusedItem = onMediaFocused,
                    firstItemFocusRequester = initialFocusRequester,
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    onEpisodeSelected = onEpisodeSelected
                )
            }

            if (hasVisibleContent) {
                item(key = "tv-home-trailing-space") {
                    Spacer(modifier = Modifier.height(TvHomeBringIntoViewTrailingSpace))
                }
            }
        }
    }
}
