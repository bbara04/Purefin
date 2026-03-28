package hu.bbara.purefin.tv.home.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
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
import hu.bbara.purefin.feature.shared.home.ContinueWatchingItem
import hu.bbara.purefin.feature.shared.home.NextUpItem
import hu.bbara.purefin.feature.shared.home.PosterItem
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import kotlin.math.nextUp

@Composable
fun TvContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstItemFocusRequester = remember { FocusRequester() }
    LaunchedEffect(items.isNotEmpty()) {
        if (items.isNotEmpty()) {
            firstItemFocusRequester.requestFocus()
        }
    }
    if (items.isEmpty()) return
    TvSectionHeader(
        title = "Continue Watching",
        action = null
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(items = items) { index, item ->
            TvContinueWatchingCard(
                item = item,
                focusRequester = if (index == 0) firstItemFocusRequester else null,
                onMovieSelected = onMovieSelected,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@Composable
fun TvContinueWatchingCard(
    item: ContinueWatchingItem,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onMovieSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val density = LocalDensity.current
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.07f else 1.0f, label = "scale")

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
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(scheme.surfaceVariant)
        ) {
            PurefinAsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { isFocused = it.isFocused }
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
fun TvNextUpSection(
    items: List<NextUpItem>,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    TvSectionHeader(
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
            TvNextUpCard(
                item = item,
                onEpisodeSelected = onEpisodeSelected
            )
        }
    }
}

@Composable
fun TvNextUpCard(
    item: NextUpItem,
    modifier: Modifier = Modifier,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val density = LocalDensity.current
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.07f else 1.0f, label = "scale")

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
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(scheme.surfaceVariant)
        ) {
            PurefinAsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { isFocused = it.isFocused }
                    .clickable {
                        openItem(item)
                    },
                contentScale = ContentScale.Crop,
            )
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
fun TvLibraryPosterSection(
    title: String,
    items: List<PosterItem>,
    action: String?,
    modifier: Modifier = Modifier,
    onMovieSelected: (UUID) -> Unit,
    onSeriesSelected: (UUID) -> Unit,
    onEpisodeSelected: (UUID, UUID, UUID) -> Unit,
) {
    TvSectionHeader(
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
fun TvSectionHeader(
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
