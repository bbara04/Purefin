package hu.bbara.purefin.app.home.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.core.model.SearchResult
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
internal fun HomeSearchResultCard(
    item: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            PurefinAsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (item.type) {
                    BaseItemKind.MOVIE -> "Movie"
                    BaseItemKind.SERIES -> "Series"
                    else -> "Title"
                },
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
        }
    }
}
