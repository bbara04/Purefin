package hu.bbara.purefin.ui.screen.home.components

import android.util.Log
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
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.LibraryItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import hu.bbara.purefin.feature.shared.home.SuggestedItem
import hu.bbara.purefin.ui.screen.home.components.continuewatching.ContinueWatchingSection
import hu.bbara.purefin.ui.screen.home.components.featured.SuggestionsSection
import hu.bbara.purefin.ui.screen.home.components.library.LibraryPosterSection
import hu.bbara.purefin.ui.screen.home.components.nextup.NextUpSection
import java.util.UUID
import hu.bbara.purefin.core.model.MediaKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    libraries: List<LibraryItem>,
    libraryContent: Map<UUID, List<PosterItem>>,
    suggestions: List<SuggestedItem>,
    continueWatching: List<ContinueWatchingItem>,
    nextUp: List<NextUpItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    onLibrarySelected: (LibraryItem) -> Unit,
    onBrowseLibrariesClick: () -> Unit,
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
                            onItemOpen = { item ->
                                when (item.type) {
                                    MediaKind.MOVIE -> onMovieSelected(item.id)
                                    MediaKind.SERIES -> onSeriesSelected(item.id)
                                    MediaKind.EPISODE -> onEpisodeSelected(item.id, item.id, item.id)
                                    else -> {
                                        Log.e("HomeContent", "Unsupported item type: ${item.type}")
                                    }
                                }
                            }
                        )
                    }
                }

                if (continueWatching.isNotEmpty()) {
                    item(key = "continue-watching") {
                        ContinueWatchingSection(
                            items = continueWatching,
                            onMovieSelected = onMovieSelected,
                            onEpisodeSelected = onEpisodeSelected
                        )
                    }
                }

                if (nextUp.isNotEmpty()) {
                    item(key = "next-up") {
                        NextUpSection(
                            items = nextUp,
                            onEpisodeSelected = onEpisodeSelected
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
                        onMovieSelected = onMovieSelected,
                        onSeriesSelected = onSeriesSelected,
                        onEpisodeSelected = onEpisodeSelected
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