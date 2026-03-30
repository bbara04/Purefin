package hu.bbara.purefin.app.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage

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

@Composable
fun LibraryListItem(
    item: HomeNavItem,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PurefinAsyncImage(
            model = item.posterUrl,
            contentDescription = item.label,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.displaySmall
        )
    }
}
