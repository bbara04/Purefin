package hu.bbara.purefin.ui.screen.series.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.ui.common.media.MediaCastRow
import hu.bbara.purefin.ui.common.media.MediaMetadataFlowRow
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.common.button.MediaActionButton
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.button.MediaResumeButton
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.download.DownloadState
import hu.bbara.purefin.image.ImageUrlBuilder
import hu.bbara.purefin.navigation.EpisodeDto
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.feature.content.series.SeriesViewModel
import hu.bbara.purefin.player.PlayerActivity
import hu.bbara.purefin.image.ArtworkKind

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

@Composable
internal fun SeriesMetaChips(series: Series) {
    MediaMetadataFlowRow(
        items = listOf(series.year, "${series.seasonCount} Seasons")
    )
}

@Composable
internal fun SeriesActionButtons(
    nextUpEpisode: Episode?,
    seriesDownloadState: DownloadState,
    selectedSeason: Season,
    seasonDownloadState: DownloadState,
    onDownloadOptionSelected: (SeriesDownloadOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val navigationManager = LocalNavigationManager.current
    val scheme = MaterialTheme.colorScheme
    var showDownloadDialog by remember { mutableStateOf(false) }
    val episodeId = nextUpEpisode?.id
    val playAction = remember(nextUpEpisode) {
        nextUpEpisode?.let { episode ->
            {
                navigationManager.navigate(
                    Route.EpisodeRoute(
                        EpisodeDto(
                            id = episode.id,
                            seasonId = episode.seasonId,
                            seriesId = episode.seriesId
                        )
                    )
                )
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("MEDIA_ID", episode.id.toString())
                context.startActivity(intent)
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (playAction != null && nextUpEpisode != null) {
            MediaResumeButton(
                text = mediaPlayButtonText(nextUpEpisode.progress, nextUpEpisode.watched),
                progress = mediaPlaybackProgress(nextUpEpisode.progress),
                onClick = playAction,
                modifier = Modifier.sizeIn(maxWidth = 200.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        MediaActionButton(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            iconColor = MaterialTheme.colorScheme.onSecondary,
            icon = Icons.Outlined.Add,
            height = 48.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        MediaActionButton(
            backgroundColor = scheme.secondary,
            iconColor = scheme.onSecondary,
            icon = when {
                seriesDownloadState is DownloadState.Downloading -> Icons.Outlined.Close
                seriesDownloadState is DownloadState.Downloaded -> Icons.Outlined.DownloadDone
                else -> Icons.Outlined.Download
            },
            height = 48.dp,
            onClick = { showDownloadDialog = true }
        )
    }

    if (showDownloadDialog) {
        DownloadOptionsDialog(
            selectedSeasonName = selectedSeason.name,
            seasonDownloadState = seasonDownloadState,
            onDownloadOptionSelected = {
                showDownloadDialog = false
                onDownloadOptionSelected(it)
            },
            onDismiss = { showDownloadDialog = false }
        )
    }
}

internal enum class SeriesDownloadOption {
    SEASON,
    SERIES,
    SMART
}

@Composable
private fun DownloadOptionsDialog(
    selectedSeasonName: String,
    seasonDownloadState: DownloadState,
    onDownloadOptionSelected: (SeriesDownloadOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Choose how to download this series.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onDownloadOptionSelected(SeriesDownloadOption.SEASON) }) {
                    Text(
                        when (seasonDownloadState) {
                            is DownloadState.Downloaded -> "$selectedSeasonName Downloaded"
                            is DownloadState.Downloading -> "Downloading $selectedSeasonName"
                            else -> "Download $selectedSeasonName"
                        }
                    )
                }
                TextButton(onClick = { onDownloadOptionSelected(SeriesDownloadOption.SERIES) }) {
                    Text("Download All")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDownloadOptionSelected(SeriesDownloadOption.SMART) }) {
                Text("Smart Download")
            }
        }
    )
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
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
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
        items(episodes, key = { episode -> episode.id }) { episode ->
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
    MediaCastRow(
        cast = cast,
        modifier = modifier,
        cardWidth = 84.dp,
        nameSize = 11.sp,
        roleSize = 10.sp
    )
}
