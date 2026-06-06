package hu.bbara.purefin.ui.common.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaMetadataFlowRow(
    items: List<String>,
    highlightedItem: String? = null,
    highlightedBackground: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    highlightedBorder: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    highlightedTextColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.filter { it.isNotBlank() }.forEach { item ->
            if (item == highlightedItem) {
                MediaMetaChip(
                    text = item,
                    background = highlightedBackground,
                    border = highlightedBorder,
                    textColor = highlightedTextColor
                )
            } else {
                MediaMetaChip(text = item)
            }
        }
    }
}
