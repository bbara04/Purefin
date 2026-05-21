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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import hu.bbara.purefin.ui.screen.library.components.LibraryTopBar

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

    Scaffold(
        modifier = modifier.testTag(LibraryScreenTag),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            LibraryTopBar(
                onBack = { viewModel.onBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LibraryPosterGrid(libraryItems = libraryItems.value)
        }
    }
}

@Composable
internal fun LibraryPosterGrid(
    libraryItems: List<MediaUiModel>,
    modifier: Modifier = Modifier,
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
