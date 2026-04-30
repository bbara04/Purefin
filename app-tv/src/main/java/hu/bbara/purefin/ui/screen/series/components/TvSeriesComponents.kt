package hu.bbara.purefin.ui.screen.series.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.image.ArtworkKind
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.ui.screen.home.components.TvHomeRowBringIntoViewSpec
import java.util.UUID

internal const val SeriesFirstSeasonTabTag = "series-first-season-tab"
internal const val SeriesNextUpEpisodeCardTag = "series-next-up-episode-card"

@Composable
internal fun TvSeriesMetaChips(series: Series) {
    MediaMetadataFlowRow(
        items = listOf(series.year, "${series.seasonCount} Seasons")
    )
}

@Composable
internal fun TvSeasonTabs(
    seasons: List<Season>,
    selectedSeason: Season?,
    modifier: Modifier = Modifier,
    firstItemFocusRequester: FocusRequester? = null,
    firstItemTestTag: String? = null,
    onSelect: (Season) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        seasons.forEachIndexed { index, season ->
            TvSeasonTab(
                name = season.name,
                isSelected = season == selectedSeason,
                onSelect = { onSelect(season) },
                modifier = Modifier
                    .then(
                        if (index == 0 && firstItemFocusRequester != null) {
                            Modifier.focusRequester(firstItemFocusRequester)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (index == 0 && firstItemTestTag != null) {
                            Modifier.testTag(firstItemTestTag)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
private fun TvSeasonTab(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    var isFocused by remember { mutableStateOf(false) }
    val color = if (isSelected || isFocused) scheme.primary else mutedStrong
    val underlineColor = if (isSelected || isFocused) scheme.primary else Color.Transparent
    val underlineHeight = if (isFocused) 3.dp else 2.dp

    Column(
        modifier = modifier
            .padding(bottom = 8.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect() }
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(underlineHeight)
                .width(52.dp)
                .background(underlineColor)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TvEpisodeCarousel(
    episodes: List<Episode>,
    onPlayEpisode: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    focusedEpisodeId: UUID? = null
) {
    val listState = rememberLazyListState()
    val focusedEpisodeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(episodes, focusedEpisodeId) {
        val focusedEpisodeIndex = focusedEpisodeId?.let { id ->
            episodes.indexOfFirst { it.id == id }
        } ?: -1
        val firstUnwatchedIndex = episodes.indexOfFirst { !it.watched }.let { if (it == -1) 0 else it }
        val targetIndex = focusedEpisodeIndex.takeIf { it >= 0 } ?: firstUnwatchedIndex

        if (targetIndex != 0) {
            listState.scrollToItem(targetIndex)
        } else {
            listState.scrollToItem(0)
        }

        if (focusedEpisodeIndex >= 0) {
            withFrameNanos { }
            focusedEpisodeFocusRequester.requestFocus()
        }
    }

    CompositionLocalProvider(LocalBringIntoViewSpec provides TvHomeRowBringIntoViewSpec) {
        LazyRow(
            state = listState,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(episodes, key = { episode -> episode.id }) { episode ->
                TvEpisodeCard(
                    episode = episode,
                    onPlayEpisode = { onPlayEpisode(episode) },
                    modifier = if (episode.id == focusedEpisodeId) {
                        Modifier
                            .focusRequester(focusedEpisodeFocusRequester)
                            .testTag(SeriesNextUpEpisodeCardTag)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}

@Composable
internal fun TvSeriesHeroSection(
    series: Series,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.82f)

    Column(
        modifier = modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = series.name,
            color = scheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        TvSeriesMetaChips(series = series)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Overview",
            color = scheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = series.synopsis,
            color = mutedStrong,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(0.4f)
        )
    }
}

@Composable
private fun TvEpisodeCard(
    episode: Episode,
    onPlayEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.07f else 1.0f, label = "scale")

    Column(
        modifier = modifier
            .width(260.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onPlayEpisode() },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surface)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            PurefinAsyncImage(
                model = ImageUrlBuilder.finishImageUrl(episode.imageUrlPrefix, ArtworkKind.PRIMARY),
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
            if (episode.watched.not() && (episode.progress ?: 0.0) > 0) {
                MediaProgressBar(
                    progress = (episode.progress ?: 0.0).toFloat().div(100),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                )
            } else {
                WatchStateBadge(
                    watched = episode.watched,
                    started = (episode.progress ?: 0.0) > 0.0,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
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
//    MediaCastRow(
//        cast = cast,
//        modifier = modifier,
//        cardWidth = 84.dp,
//        nameSize = 11.sp,
//        roleSize = 10.sp
//    )
}
