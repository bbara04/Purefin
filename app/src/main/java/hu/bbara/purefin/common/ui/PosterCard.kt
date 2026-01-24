package hu.bbara.purefin.common.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.home.HomePageViewModel
import hu.bbara.purefin.app.home.ui.PosterItem
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType

@Composable
fun PosterCard(
    item: PosterItem,
    modifier: Modifier = Modifier,
    viewModel: HomePageViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme

    fun openItem(posterItem: PosterItem) {
        when (posterItem.type) {
            BaseItemKind.MOVIE -> viewModel.onMovieSelected(posterItem.id.toString())
            BaseItemKind.SERIES -> viewModel.onSeriesSelected(posterItem.id.toString())
            BaseItemKind.EPISODE -> viewModel.onEpisodeSelected(posterItem.id.toString())
            else -> {}
        }
    }
    Column(
        modifier = Modifier
            .width(144.dp)
    ) {
        PurefinAsyncImage(
            model = viewModel.getImageUrl(item.imageItemId, ImageType.PRIMARY),
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .shadow(10.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
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
