package hu.bbara.purefin.ui.screen.series.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.series.SeriesViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.model.CastMember
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.bar.MediaProgressBar
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.common.image.PurefinAsyncImage
import hu.bbara.purefin.ui.model.MediaAction
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeriesTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    DefaultTopBar(
        leftActions = {
            GhostIconButton(
                onClick = onBack,
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back")
        },
        rightActions = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
                GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
            }
        },
        withIcon = false,
        scrollBehavior = scrollBehavior
    )
}

internal enum class SeriesDownloadOption {
    SEASON,
    SERIES,
    SMART,
    DELETE_SMART
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadOptionsBottomSheet(
    selectedSeasonName: String,
    seriesDownloadState: DownloadState,
    seasonDownloadState: DownloadState,
    isSmartDownloadEnabled: Boolean,
    onDownloadOptionSelected: (SeriesDownloadOption) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(SeriesDownloadDialogTag),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Download options",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Choose how to download this series.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DownloadOptionRow(
                title = "Download selected season",
                supportingText = when (seasonDownloadState) {
                    is DownloadState.Downloaded -> "$selectedSeasonName is already downloaded."
                    is DownloadState.Downloading -> "$selectedSeasonName is downloading."
                    else -> "Save episodes from $selectedSeasonName for offline viewing."
                },
                icon = Icons.Outlined.Download,
                onClick = { onDownloadOptionSelected(SeriesDownloadOption.SEASON) },
                enabled = seasonDownloadState is DownloadState.NotDownloaded,
                modifier = Modifier.testTag(SeriesDownloadSeasonButtonTag)
            )
            DownloadOptionRow(
                title = "Download all episodes",
                supportingText = when (seriesDownloadState) {
                    is DownloadState.Downloaded -> "All episodes in this series are already downloaded."
                    is DownloadState.Downloading -> "All episodes in this series are downloading."
                    else -> "Save every available episode in this series."
                },
                icon = Icons.Outlined.Download,
                onClick = { onDownloadOptionSelected(SeriesDownloadOption.SERIES) },
                enabled = seriesDownloadState is DownloadState.NotDownloaded,
                modifier = Modifier.testTag(SeriesDownloadAllButtonTag)
            )
            if (isSmartDownloadEnabled) {
                DownloadOptionRow(
                    title = "Delete series downloads",
                    supportingText = "Turns off Smart Downloads and removes offline episodes for this series.",
                    icon = Icons.Outlined.Delete,
                    onClick = { onDownloadOptionSelected(SeriesDownloadOption.DELETE_SMART) },
                    modifier = Modifier.testTag(SeriesSmartDownloadButtonTag),
                    destructive = true
                )
            } else {
                DownloadOptionRow(
                    title = "Smart Downloads",
                    supportingText = "Automatically keep the next unwatched episodes available offline.",
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { onDownloadOptionSelected(SeriesDownloadOption.SMART) },
                    modifier = Modifier.testTag(SeriesSmartDownloadButtonTag)
                )
            }
        }
    }
}

@Composable
private fun DownloadOptionRow(
    title: String,
    supportingText: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true,
) {
    val contentColor = if (!enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = if (!enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else if (destructive) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = supportingColor
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(enabled = enabled, onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeasonTabs(
    seasons: List<Season>,
    selectedSeason: Season?,
    modifier: Modifier = Modifier,
    onSelect: (Season) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .clickable { showBottomSheet = true }
            .testTag(SeriesSeasonSelectorTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = selectedSeason?.name ?: "Select Season",
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = "Select season",
            tint = scheme.onSurfaceVariant
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Select Season",
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                items(seasons) { season ->
                    ListItem(
                        headlineContent = {
                            Text(text = season.name)
                        },
                        leadingContent = if (season == selectedSeason) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = scheme.primary
                                )
                            }
                        } else null,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            onSelect(season)
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun EpisodeCarousel(episodes: List<Episode>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(SeriesEpisodeCarouselTag),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        episodes.forEach { episode ->
            EpisodeCard(episode = episode)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeCard(
    viewModel: SeriesViewModel = hiltViewModel(),
    episode: Episode
) {
    val scheme = MaterialTheme.colorScheme
    val mutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    var showBottomSheet by remember { mutableStateOf(false) }
    val popupActions = remember(episode.id) {
        listOf(
            MediaAction(name = "Mark as watched") {
                viewModel.markEpisodeAsWatched(episode.id, true)
            },
            MediaAction(name = "Mark as unwatched") {
                viewModel.markEpisodeAsWatched(episode.id, false)
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("$SeriesEpisodeCardTagPrefix${episode.id}")
            .combinedClickable(
                onClick = {
                    viewModel.onSelectEpisode(
                        seriesId = episode.seriesId,
                        seasonId = episode.seasonId,
                        episodeId = episode.id
                    )
                },
                onLongClick = { showBottomSheet = true }
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(132.dp)
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
            if (episode.watched.not() && (episode.progress ?: 0.0) > 0) {
                MediaProgressBar(
                    progress = (episode.progress ?: 0.0).toFloat().div(100),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                )
            } else {
                WatchStateBadge(
                    watched = episode.watched,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = episode.title,
                color = scheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Episode ${episode.index} • ${episode.runtime}",
                color = mutedStrong,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier.testTag(SeriesEpisodeActionsDialogTag),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                popupActions.forEach { action ->
                    ListItem(
                        headlineContent = { Text(text = action.name) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            action.onClick()
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}

internal const val SeriesPlayButtonTag = "series-play-button"
internal const val SeriesAddButtonTag = "series-add-button"
internal const val SeriesDownloadButtonTag = "series-download-button"
internal const val SeriesWatchedButtonTag = "series-watched-button"
internal const val SeriesDownloadDialogTag = "series-download-dialog"
internal const val SeriesDownloadSeasonButtonTag = "series-download-season-button"
internal const val SeriesDownloadAllButtonTag = "series-download-all-button"
internal const val SeriesSmartDownloadButtonTag = "series-smart-download-button"
internal const val SeriesSeasonSelectorTag = "series-season-selector"
internal const val SeriesEpisodeCarouselTag = "series-episode-carousel"
internal const val SeriesEpisodeCardTagPrefix = "series-episode-card-"
internal const val SeriesEpisodeActionsDialogTag = "series-episode-actions-dialog"

@Composable
internal fun CastRow(cast: List<CastMember>, modifier: Modifier = Modifier) {
    //TODO fix
//    MediaCastRow(
//        cast = cast,
//        modifier = modifier,
//        cardWidth = 84.dp,
//        nameSize = 11.sp,
//        roleSize = 10.sp
//    )
}
