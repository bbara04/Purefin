package hu.bbara.purefin.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.request.ImageRequest
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun PosterCard(
    item: PosterItem,
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current

    val posterWidth = 144.dp
    val posterHeight = posterWidth * 3 / 2

    fun openItem(posterItem: PosterItem) {
        when (posterItem.type) {
            BaseItemKind.MOVIE -> onMovieSelected(posterItem.id)
            BaseItemKind.SERIES -> onSeriesSelected(posterItem.id)
            BaseItemKind.EPISODE -> onEpisodeSelected(
                posterItem.episode!!.seriesId,
                posterItem.episode.seasonId,
                posterItem.episode.id
            )
            else -> {}
        }
    }

    val imageRequest = ImageRequest.Builder(context)
        .data(item.imageUrl)
        .size(with(density) { posterWidth.roundToPx() }, with(density) { posterHeight.roundToPx() })
        .build()
    Column(
        modifier = Modifier
            .width(posterWidth)
    ) {
        PurefinAsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, scheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .background(scheme.surfaceVariant)
                .clickable(onClick = { openItem(item) }),
            contentScale = ContentScale.Crop
        )
        Text(
            text = item.title,
            color = scheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
