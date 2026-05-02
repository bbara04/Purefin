package hu.bbara.purefin.ui.screen.libraries.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.model.LibraryUiModel

@Composable
fun LibrariesContent(
    items: List<LibraryUiModel>,
    onLibrarySelected: (LibraryUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val minCellSize = if (maxWidth >= 600.dp) 220.dp else 160.dp

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellSize),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items, key = { it.id }) { item ->
                LibraryListItem(
                    uiModel = item,
                    onClick = { onLibrarySelected(item) }
                )
            }
        }
    }
}
