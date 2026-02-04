package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.components.GhostIconButton
import hu.bbara.purefin.common.ui.components.MediaActionButton
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.common.ui.components.WatchStateIndicator
import hu.bbara.purefin.data.model.CastMember
import hu.bbara.purefin.data.model.Episode
import hu.bbara.purefin.data.model.Season
import hu.bbara.purefin.data.model.Series

@Composable
internal fun SeriesTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GhostIconButton(
            onClick = onBack,
            icon = Icons.Outlined.ArrowBack,
            contentDescription = "Back")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SeriesMetaChips(series: Series) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MediaMetaChip(text = series.year)
//        MediaMetaChip(text = series.rating)
        MediaMetaChip(text = "${series.seasonCount} Seasons")
//        MediaMetaChip(
//            text = series.,
//            background = scheme.primary.copy(alpha = 0.2f),
//            border = scheme.primary.copy(alpha = 0.3f),
//            textColor = scheme.primary
//        )
    }
}

@Composable
internal fun SeriesActionButtons(modifier: Modifier = Modifier) {
    Row() {
        MediaActionButton(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            iconColor = MaterialTheme.colorScheme.onSecondary,
            icon = Icons.Outlined.Add,
            height = 32.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        MediaActionButton(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            iconColor = MaterialTheme.colorScheme.onSecondary,
            icon = Icons.Outlined.Download,
            height = 32.dp
        )
    }
}

@Composable
internal fun SeasonTabs(
    seasons: List<Season>,
    selectedSeason: Season?,
    modifier: Modifier = Modifier,
    onSelect: (Season) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        seasons.forEach { season ->
            SeasonTab(
                name = season.name,
                isSelected = season == selectedSeason,
                modifier = Modifier.clickable { onSelect(season) }
            )
        }
    }
}

@Composable
private fun SeasonTab(
    name: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val color = if (isSelected) scheme.primary else mutedStrong
    val borderColor = if (isSelected) scheme.primary else Color.Transparent
    Column(
        modifier = modifier
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(52.dp)
                .background(borderColor)
        )
    }
}

@Composable
internal fun EpisodeCarousel(episodes: List<Episode>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(episodes) {
        val firstUnwatchedIndex = episodes.indexOfFirst { !it.watched }.let { if (it == -1) 0 else it }
        if (firstUnwatchedIndex != 0) {
            listState.animateScrollToItem(firstUnwatchedIndex)
        } else {
            listState.scrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(episodes) { episode ->
            EpisodeCard(episode = episode)
        }
    }
}

@Composable
private fun EpisodeCard(
    viewModel: SeriesViewModel = hiltViewModel(),
    episode: Episode
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    Column(
        modifier = Modifier
            .width(260.dp)
            .clickable { viewModel.onSelectEpisode(
                seriesId = episode.seriesId,
                seasonId = episode.seasonId,
                episodeId = episode.id
            ) },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surface)
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            PurefinAsyncImage(
                model = episode.heroImageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(scheme.background.copy(alpha = 0.2f))
            )
            Icon(
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = null,
                tint = scheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(scheme.background.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = episode.runtime,
                    color = scheme.onBackground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            WatchStateIndicator(
                watched = episode.watched,
                started = (episode.progress ?: 0.0) > 0.0,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
        Column(
        ) {
            Text(
                text = episode.title,
                color = scheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Episode ${episode.index}",
                color = mutedStrong,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun CastRow(cast: List<CastMember>, modifier: Modifier = Modifier) {
    MediaCastRow(
        cast = cast,
        modifier = modifier,
        cardWidth = 84.dp,
        nameSize = 11.sp,
        roleSize = 10.sp
    )
}
