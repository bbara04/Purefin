package hu.bbara.purefin.app.home.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import hu.bbara.purefin.common.ui.PosterCard
import hu.bbara.purefin.common.ui.components.MediaProgressBar
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.player.PlayerActivity
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import kotlin.math.nextUp

@Composable
fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = "Continue Watching",
        action = null
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = items) { item ->
            ContinueWatchingCard(
                item = item,
                onMovieSelected = onMovieSelected,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val density = LocalDensity.current

    val imageUrl = when (item.type) {
        BaseItemKind.MOVIE -> item.movie?.heroImageUrl
        BaseItemKind.EPISODE -> item.episode?.heroImageUrl
        else -> null
    }

    val cardWidth = 280.dp
    val cardHeight = cardWidth * 9 / 16

    fun openItem(item: ContinueWatchingItem) {
        when (item.type) {
            BaseItemKind.MOVIE -> onMovieSelected(item.movie!!.id)
            BaseItemKind.EPISODE -> {
                val episode = item.episode!!
                onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
            }

            else -> {}
        }
    }

    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .size(with(density) { cardWidth.roundToPx() }, with(density) { cardHeight.roundToPx() })
        .build()

    Column(
        modifier = modifier
            .width(cardWidth)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, scheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(scheme.surfaceVariant)
        ) {
            PurefinAsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        openItem(item)
                    },
                contentScale = ContentScale.Crop,
            )
            MediaProgressBar(
                progress = item.progress.toFloat().nextUp().div(100),
                foregroundColor = scheme.onSurface,
                backgroundColor = scheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
            )
            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 16.dp)
                    .clip(CircleShape)
                    .background(scheme.secondary)
                    .size(36.dp),
                onClick = {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("MEDIA_ID", item.id.toString())
                    context.startActivity(intent)
                },
                colors = IconButtonColors(
                    containerColor = scheme.secondary,
                    contentColor = scheme.onSecondary,
                    disabledContainerColor = scheme.secondary,
                    disabledContentColor = scheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = item.primaryText,
                color = scheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.secondaryText,
                color = scheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NextUpSection(
    items: List<NextUpItem>,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionHeader(
        title = "Next Up",
        action = null
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = items, key = { it.id }) { item ->
            NextUpCard(
                item = item,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@Composable
fun NextUpCard(
    item: NextUpItem,
    modifier: Modifier = Modifier,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val density = LocalDensity.current

    val imageUrl = item.episode.heroImageUrl

    val cardWidth = 280.dp
    val cardHeight = cardWidth * 9 / 16

    fun openItem(item: NextUpItem) {
        val episode = item.episode
        onEpisodeSelected(episode.seriesId, episode.seasonId, episode.id)
    }

    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .size(with(density) { cardWidth.roundToPx() }, with(density) { cardHeight.roundToPx() })
        .build()

    Column(
        modifier = modifier
            .width(cardWidth)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, scheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(scheme.surfaceVariant)
        ) {
            PurefinAsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        openItem(item)
                    },
                contentScale = ContentScale.Crop,
            )
            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 16.dp)
                    .clip(CircleShape)
                    .background(scheme.secondary)
                    .size(36.dp),
                onClick = {
                    val intent = Intent(context, PlayerActivity::class.java)
                    intent.putExtra("MEDIA_ID", item.id.toString())
                    context.startActivity(intent)
                },
                colors = IconButtonColors(
                    containerColor = scheme.secondary,
                    contentColor = scheme.onSecondary,
                    disabledContainerColor = scheme.secondary,
                    disabledContentColor = scheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = item.primaryText,
                color = scheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.secondaryText,
                color = scheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    SectionHeader(
        title = title,
        action = action
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
                onMovieSelected = onMovieSelected,
                onSeriesSelected = onSeriesSelected,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String?,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = scheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (action != null) {
            Text(
                text = action,
                color = scheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick() })
        }
    }
}
