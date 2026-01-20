package hu.bbara.purefin.app.home.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import hu.bbara.purefin.app.home.HomePageViewModel
import hu.bbara.purefin.common.ui.PosterCard
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.player.PlayerActivity
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import kotlin.math.nextUp

@Composable
fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>, colors: HomeColors, modifier: Modifier = Modifier
) {
    SectionHeader(
        title = "Continue Watching", action = null, colors = colors
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = items, key = { it.id }) { item ->
            ContinueWatchingCard(
                item = item, colors = colors
            )
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    colors: HomeColors,
    modifier: Modifier = Modifier,
    viewModel: HomePageViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    fun openItem(item: ContinueWatchingItem) {
        when (item.type) {
            BaseItemKind.MOVIE -> viewModel.onMovieSelected(item.id.toString())
            BaseItemKind.EPISODE -> viewModel.onEpisodeSelected(item.id.toString())
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .width(280.dp)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
        ) {
            AsyncImage(
                model = JellyfinImageHelper.toImageUrl(
                    url = "https://jellyfin.bbara.hu", itemId = item.id, type = ImageType.PRIMARY
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        openItem(item)
                    },
                contentScale = ContentScale.Crop,

                )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.progress.toFloat().nextUp().div(100))
                        .background(colors.primary)
                )
            }
            Button(
                modifier = Modifier.align(Alignment.BottomEnd), onClick = {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("MEDIA_ID", item.id.toString())
                    context.startActivity(intent)
                }) {
                Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = "Play")
            }
        }
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = item.primaryText,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.secondaryText,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LibraryPosterSection(
    title: String,
    items: List<PosterItem>,
    action: String?,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = title, action = action, colors = colors
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = items, key = { it.id }) { item ->
            PosterCard(
                item = item,
                colors = colors,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String?,
    colors: HomeColors,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold
        )
        if (action != null) {
            Text(
                text = action,
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick() })
        }
    }
}
