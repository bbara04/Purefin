package hu.bbara.purefin.ui.screen.home.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.feature.browse.home.LibraryItem
import hu.bbara.purefin.ui.common.header.SectionHeader

@Composable
fun LibraryPosterSection(
    library: LibraryItem,
    items: List<MediaUiModel>,
    onLibrarySelected: (LibraryItem) -> Unit,
    onMediaSelected: (MediaUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val listState = rememberLazyListState()

    LaunchedEffect(items) {
        listState.scrollToItem(index = 0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(
            title = library.name,
            actionLabel = "See all",
            onActionClick = { onLibrarySelected(library) }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
            state = listState
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                HomeBrowseCard(
                    uiModel = item,
                    onMediaSelected = onMediaSelected
                )
            }
        }
    }
}
