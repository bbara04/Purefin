package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.model.LibraryUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.screen.home.components.continuewatching.ContinueWatchingSection
import hu.bbara.purefin.ui.screen.home.components.featured.SuggestionsSection
import hu.bbara.purefin.ui.screen.home.components.library.LibraryPosterSection
import hu.bbara.purefin.ui.screen.home.components.nextup.NextUpSection
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    libraries: List<LibraryUiModel>,
    libraryContent: Map<UUID, List<MediaUiModel>>,
    suggestions: List<MediaUiModel>,
    continueWatching: List<MediaUiModel>,
    nextUp: List<MediaUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMediaSelected: (MediaUiModel) -> Unit,
    onLibrarySelected: (LibraryUiModel) -> Unit,
    onBrowseLibrariesClick: () -> Unit,
    onMarkAsWatched: (MediaUiModel, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val visibleLibraries = remember(libraries, libraryContent) {
        libraries.filter { libraryContent[it.id].orEmpty().isNotEmpty() }
    }
    val listState = rememberLazyListState()
    var pendingInitialSuggestionsReveal by rememberSaveable { mutableStateOf(suggestions.isEmpty()) }
    var userInteractedBeforeSuggestionsLoaded by rememberSaveable { mutableStateOf(false) }

    val hasContent = libraryContent.isNotEmpty() || continueWatching.isNotEmpty() || nextUp.isNotEmpty() || suggestions.isNotEmpty()

    LaunchedEffect(listState, pendingInitialSuggestionsReveal) {
        if (!pendingInitialSuggestionsReveal) return@LaunchedEffect
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    userInteractedBeforeSuggestionsLoaded = true
                }
            }
    }

    LaunchedEffect(
        suggestions.isNotEmpty(),
        pendingInitialSuggestionsReveal,
        userInteractedBeforeSuggestionsLoaded
    ) {
        if (!suggestions.isNotEmpty() || !pendingInitialSuggestionsReveal) return@LaunchedEffect
        if (!userInteractedBeforeSuggestionsLoaded) {
            listState.scrollToItem(0)
        }
        pendingInitialSuggestionsReveal = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    item(key = "featured") {
                        SuggestionsSection(
                            items = suggestions,
                            onItemOpen = { item -> onMediaSelected(item) }
                        )
                    }
                }

                if (continueWatching.isNotEmpty()) {
                    item(key = "continue-watching") {
                        ContinueWatchingSection(
                            items = continueWatching,
                            onMarkAsWatched = onMarkAsWatched,
                            onMediaSelected = onMediaSelected
                        )
                    }
                }

                if (nextUp.isNotEmpty()) {
                    item(key = "next-up") {
                        NextUpSection(
                            items = nextUp,
                            onMediaSelected = onMediaSelected
                        )
                    }
                }

                items(
                    items = visibleLibraries,
                    key = { library -> library.id }
                ) { library ->
                    LibraryPosterSection(
                        library = library,
                        items = libraryContent[library.id].orEmpty(),
                        onLibrarySelected = onLibrarySelected,
                        onMediaSelected = onMediaSelected
                    )
                }

                if (!hasContent) {
                    item(key = "empty-state") {
                        HomeEmptyState(
                            onRefresh = onRefresh,
                            onBrowseLibrariesClick = onBrowseLibrariesClick
                        )
                    }
                }
            }
        }
    }
}