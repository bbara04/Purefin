package hu.bbara.purefin.app.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.app.library.LibraryViewModel
import hu.bbara.purefin.common.ui.PosterCard
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


    LibraryPosterGrid(libraryItems = libraryItems.value)
}

@Composable
fun LibraryPosterGrid(
    libraryItems: List<PosterItem>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.background(Color.Black)
    ) {
        items(libraryItems) { item ->
            PosterCard(
                item = item,

            )
        }
    }
}