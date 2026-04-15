package hu.bbara.purefin.ui.screen.home.components.nextup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.header.SectionHeader
import hu.bbara.purefin.feature.browse.home.NextUpItem
import java.util.UUID

@Composable
fun NextUpSection(
    items: List<NextUpItem>,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        SectionHeader(title = "Next Up")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = items, key = { item -> item.id }) { item ->
                NextUpCard(
                    item = item,
                    onEpisodeSelected = onEpisodeSelected
                )
            }
        }
    }
}
