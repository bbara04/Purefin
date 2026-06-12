package hu.bbara.purefin.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.feature.browse.library.LibraryViewModel
import hu.bbara.purefin.core.model.EpisodeUiModel
import hu.bbara.purefin.core.model.MediaUiModel
import hu.bbara.purefin.core.model.MovieUiModel
import hu.bbara.purefin.core.model.SeriesUiModel
import hu.bbara.purefin.core.navigation.LibraryDto
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.model.MediaAction
import hu.bbara.purefin.ui.screen.home.components.rememberDefaultTopBarScrollBehavior
import hu.bbara.purefin.ui.screen.library.components.LibraryTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    library: LibraryDto,
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(library) {
        viewModel.selectLibrary(libraryId = library.id)
    }

    val libraryItems = viewModel.contents.collectAsStateWithLifecycle()
    val topBarScrollBehavior = rememberDefaultTopBarScrollBehavior()

    Scaffold(
        modifier = modifier
            .testTag(LibraryScreenTag)
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LibraryTopBar(
                onBack = { viewModel.onBack() },
                scrollBehavior = topBarScrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LibraryPosterGrid(
                libraryItems = libraryItems.value,
                onMarkAsWatched = viewModel::markAsWatched,
            )
        }
    }
}

@Composable
internal fun LibraryPosterGrid(
    libraryItems: List<MediaUiModel>,
    modifier: Modifier = Modifier,
    onMarkAsWatched: (MediaUiModel, Boolean) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    BoxWithConstraints(
        modifier = modifier
            .testTag(LibraryPosterGridTag)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val minCellSize = if (maxWidth >= 600.dp) 220.dp else 120.dp

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellSize),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(libraryItems, key = { item -> item.id }) { item ->
                val popupActions = remember {
                    buildList {
                        if (item is MovieUiModel || item is EpisodeUiModel || item is SeriesUiModel) {
                            add(MediaAction(name = "Mark as watched") { onMarkAsWatched(item, true) })
                            add(MediaAction(name = "Mark as unwatched") { onMarkAsWatched(item, false) })
                        }
                    }
                }

                MediaImageCard(
                    imageUrl = item.primaryImageUrl,
                    title = item.primaryText,
                    subtitle = item.secondaryText,
                    onClick = {
                        when (item) {
                            is MovieUiModel -> viewModel.onMovieSelected(item.id)
                            is SeriesUiModel -> viewModel.onSeriesSelected(item.id)
                            is EpisodeUiModel -> Unit
                        }
                    },
                    popupActions = popupActions,
                    modifier = Modifier.testTag("$LibraryPosterItemTagPrefix${item.id}")
                ) {
                    when (item) {
                        is MovieUiModel, is EpisodeUiModel -> {
                            WatchStateBadge(
                                size = 28,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                watched = item.watched,
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

internal const val LibraryScreenTag = "library-screen"
internal const val LibraryPosterGridTag = "library-poster-grid"
internal const val LibraryPosterItemTagPrefix = "library-poster-item-"
