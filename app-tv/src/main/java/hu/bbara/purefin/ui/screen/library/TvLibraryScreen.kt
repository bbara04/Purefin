package hu.bbara.purefin.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.ui.common.card.PosterCard
import hu.bbara.purefin.ui.common.button.PurefinIconButton
import hu.bbara.purefin.core.navigation.LibraryDto
import hu.bbara.purefin.feature.browse.home.PosterItem
import hu.bbara.purefin.feature.browse.library.LibraryViewModel
import java.util.UUID

@Composable
fun TvLibraryScreen(
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
        topBar = {
            TvLibraryTopBar(
                title = library.name,
                onBack = viewModel::onBack
            )
        }
    ) { innerPadding ->
        TvLibraryContent(
            libraryItems = libraryItems.value,
            onMovieSelected = viewModel::onMovieSelected,
            onSeriesSelected = viewModel::onSeriesSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun TvLibraryTopBar(
    title: String,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PurefinIconButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            focusedScale = 1.1f,
            focusedBorderWidth = 2.5.dp,
            focusedBorderColor = scheme.onPrimary,
            focusedBackgroundColor = scheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun TvLibraryContent(
    libraryItems: List<PosterItem>,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            items(libraryItems, key = { item -> item.id }) { item ->
                PosterCard(
                    item = item,
                    onMovieSelected = onMovieSelected,
                    onSeriesSelected = onSeriesSelected,
                    onEpisodeSelected = { _, _, _ -> }
                )
            }
        }
    }
}
