package hu.bbara.purefin.app.home.ui.libraries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.app.home.ui.HomeNavItem

@Composable
fun LibrariesContent(
    items: List<HomeNavItem>,
    onLibrarySelected: (HomeNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items, key = { it.id }) { item ->
            LibraryListItem(
                item = item,
                modifier = Modifier.clickable {
                    onLibrarySelected(item)
                }
            )
        }
    }
}
