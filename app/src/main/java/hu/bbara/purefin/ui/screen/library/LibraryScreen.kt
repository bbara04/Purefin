package hu.bbara.purefin.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.browse.library.LibraryViewModel
import hu.bbara.purefin.navigation.LibraryDto
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.model.EpisodeUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.model.MovieUiModel
import hu.bbara.purefin.ui.model.SeriesUiModel
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

    val libraryItems = viewModel.contents.collectAsState()

    Scaffold(
        modifier = modifier,
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.background(MaterialTheme.colorScheme.background)
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
                }
            ) {
                when (item) {
                    is MovieUiModel, is EpisodeUiModel -> {
                        WatchStateBadge(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = item.watched,
                            started = (item.progress ?: 0f) > 0f
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}
