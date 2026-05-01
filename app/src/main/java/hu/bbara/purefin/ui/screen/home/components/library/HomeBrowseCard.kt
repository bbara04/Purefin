package hu.bbara.purefin.ui.screen.home.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.model.EpisodeUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.model.MovieUiModel

@Composable
internal fun HomeBrowseCard(
    uiModel: MediaUiModel,
    onMediaSelected: (MediaUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = { onMediaSelected(uiModel) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainer),
        modifier = modifier.width(188.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(scheme.surface)
            ) {
                PurefinAsyncImage(
                    model = uiModel.primaryImageUrl,
                    contentDescription = uiModel.primaryText,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                when (uiModel) {
                    is MovieUiModel, is EpisodeUiModel -> {
                        WatchStateBadge(
                            size = 28,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            watched = uiModel.watched,
                            started = (uiModel.progress ?: 0f) > 0f
                        )
                    }
                    else -> Unit
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = uiModel.primaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiModel.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
