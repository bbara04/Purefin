package hu.bbara.purefin.app.library.ui

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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.app.library.LibraryViewModel
import hu.bbara.purefin.common.ui.PosterCard
import hu.bbara.purefin.common.ui.components.PurefinIconButton
import hu.bbara.purefin.navigation.LibraryDto

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
        topBar = {
            LibraryTopBar(
                onBack = { viewModel.onBack() }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LibraryPosterGrid(libraryItems = libraryItems.value)
        }
    }
}

@Composable
internal fun LibraryTopBar(
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        PurefinIconButton(
            icon = Icons.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack
        )
    }
}

@Composable
internal fun LibraryPosterGrid(
    libraryItems: List<PosterItem>,
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
        items(libraryItems) { item ->
            PosterCard(
                item = item,
                onMovieSelected = viewModel::onMovieSelected,
                onSeriesSelected = viewModel::onSeriesSelected,
                onEpisodeSelected = { _, _, _ -> }
            )
        }
    }
}
