package hu.bbara.purefin.ui.screen.home.components.continuewatching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.header.SectionHeader
import hu.bbara.purefin.ui.common.media.homeMediaSharedBoundsKey
import hu.bbara.purefin.ui.model.MediaUiModel

@Composable
fun ContinueWatchingSection(
    items: List<MediaUiModel>,
    onMediaSelected: (MediaUiModel) -> Unit,
    onMarkAsWatched: (MediaUiModel, Boolean) -> Unit,
    modifier: Modifier = Modifier
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
        SectionHeader(title = "Continue Watching")
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            state = listState
        ) {
            itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
                ContinueWatchingCard(
                    item = item,
                    sharedBoundsKey = homeMediaSharedBoundsKey("continue-$index", item.id),
                    onMediaSelected = onMediaSelected,
                    onMarkAsWatched = onMarkAsWatched
                )
            }
        }
    }
}
