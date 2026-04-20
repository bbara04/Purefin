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
    onMediaSelected: (MediaUiModel) -> Unit,
    contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val topOffsetPx = with(LocalDensity.current) { TvHomeFocusedItemTopOffset.toPx() }
    val hasContinueWatching = continueWatching.isNotEmpty()
    val hasNextUp = nextUp.isNotEmpty()
    val hasLibraryItems = libraries.any { libraryContent[it.id].orEmpty().isNotEmpty() }
    val firstLibraryWithItemsId = libraries.firstOrNull {
        libraryContent[it.id].orEmpty().isNotEmpty()
    }?.id
    val hasVisibleContent = hasContinueWatching || hasNextUp
    val hasInitialFocusableItem = hasContinueWatching || hasNextUp || hasLibraryItems
    val initialFocusRequester = remember { FocusRequester() }
    var initialFocusApplied by remember { mutableStateOf(false) }

    LaunchedEffect(hasInitialFocusableItem, initialFocusApplied) {
        if (!initialFocusApplied && hasInitialFocusableItem) {
            initialFocusRequester.requestFocus()
            initialFocusApplied = true
        }
    }

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
                        onMediaSelected = onMediaSelected,
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
                        onMediaSelected = onMediaSelected,
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
                    firstItemFocusRequester = initialFocusRequester.takeIf {
                        !hasContinueWatching && !hasNextUp && library.id == firstLibraryWithItemsId
                    },
                    firstItemTestTag = tvHomeLibraryFirstItemTag(library.id),
                    onMediaSelected = onMediaSelected
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
