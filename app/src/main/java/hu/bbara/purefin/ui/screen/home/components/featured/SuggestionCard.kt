package hu.bbara.purefin.ui.screen.home.components.featured

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.ui.model.MediaUiModel
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage

@Composable
internal fun SuggestionCard(
    item: MediaUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val description = item.description.trim()

    Surface(
        color = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(30.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 11f)
        ) {
            PurefinAsyncImage(
                model = item.primaryImageUrl,
                contentDescription = item.primaryText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.44f),
                                Color.Black.copy(alpha = 0.44f),
                                Color.Black.copy(alpha = 0.80f)
                            )
                        )
                    )
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = item.primaryText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.secondaryText,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 520.dp)
                    )
                }
            }
            if (item.progress != null && item.progress!! > 0f) {
                MediaProgressBar(
                    progress = item.progress!!.coerceIn(0f, 1f),
                    foregroundColor = scheme.primary,
                    backgroundColor = Color.White.copy(alpha = 0.26f),
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(bottom = 6.dp, start = 6.dp, end = 6.dp)
                )
            }
        }
    }
}
