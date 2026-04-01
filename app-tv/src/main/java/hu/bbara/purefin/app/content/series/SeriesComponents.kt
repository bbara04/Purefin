package hu.bbara.purefin.app.content.series

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar
import hu.bbara.purefin.common.ui.components.MediaProgressBar
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.common.ui.components.PurefinAsyncImage
import hu.bbara.purefin.common.ui.components.WatchStateIndicator
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.content.series.SeriesViewModel
import org.jellyfin.sdk.model.api.ImageType

internal const val SeriesPlayButtonTag = "series-play-button"
internal const val SeriesFirstSeasonTabTag = "series-first-season-tab"

@Composable
internal fun SeriesTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backFocusRequester: FocusRequester? = null,
    downFocusRequester: FocusRequester? = null
) {
    MediaDetailsTopBar(
        onBack = onBack,
        modifier = modifier,
        backFocusRequester = backFocusRequester,
        downFocusRequester = downFocusRequester
    )
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
        MediaMetaChip(text = "${series.seasonCount} Seasons")
    }
}

@Composable
internal fun SeasonTabs(
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
            SeasonTab(
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
private fun SeasonTab(
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
internal fun SeriesHeroSection(
    series: Series,
    nextUpEpisode: Episode?,
    onPlayEpisode: (Episode) -> Unit,
    playFocusRequester: FocusRequester,
    firstContentFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.82f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
    ) {
        Text(
            text = series.name,
            color = scheme.onBackground,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(18.dp))
        SeriesMetaChips(series = series)
        Spacer(modifier = Modifier.height(24.dp))
        if (nextUpEpisode != null) {
            Text(
                text = nextUpEpisode.heroStatusText(),
                color = scheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = nextUpEpisode.title,
                color = scheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Episode ${nextUpEpisode.index} • ${nextUpEpisode.runtime}",
                color = mutedStrong,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(24.dp))
            MediaResumeButton(
                text = nextUpEpisode.playButtonText(),
                progress = nextUpEpisode.progress?.div(100)?.toFloat() ?: 0f,
                onClick = { onPlayEpisode(nextUpEpisode) },
                modifier = Modifier
                    .sizeIn(minWidth = 216.dp, maxWidth = 240.dp)
                    .focusRequester(playFocusRequester)
                    .focusProperties { down = firstContentFocusRequester }
                    .testTag(SeriesPlayButtonTag)
            )
        } else {
            Text(
                text = "Choose a season below to start watching.",
                color = mutedStrong,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
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
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.07f else 1.0f, label = "scale")

    Column(
        modifier = Modifier
            .width(260.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .onFocusChanged { isFocused = it.isFocused }
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
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) scheme.primary else scheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            PurefinAsyncImage(
                model = JellyfinImageHelper.finishImageUrl(episode.imageUrlPrefix, ImageType.PRIMARY),
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
                WatchStateIndicator(
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
    MediaCastRow(
        cast = cast,
        modifier = modifier,
        cardWidth = 84.dp,
        nameSize = 11.sp,
        roleSize = 10.sp
    )
}

private fun Episode.playButtonText(): String {
    return if ((progress ?: 0.0) > 0.0 && !watched) "Resume" else "Play"
}

private fun Episode.heroStatusText(): String {
    return if ((progress ?: 0.0) > 0.0 && !watched) "Continue Watching" else "Up Next"
}
