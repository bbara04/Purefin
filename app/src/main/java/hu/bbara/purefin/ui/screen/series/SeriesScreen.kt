package hu.bbara.purefin.ui.screen.series

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.core.data.SmartDownloadStore
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.series.SeriesViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.EpisodeDto
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.navigation.LocalNavigationManager
import hu.bbara.purefin.player.PlayerActivity
import hu.bbara.purefin.ui.common.media.MediaDetailActionsUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailCastUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailPrimaryActionUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailScaffold
import hu.bbara.purefin.ui.common.media.MediaDetailScaffoldUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailSecondaryActionUiModel
import hu.bbara.purefin.ui.common.media.MediaDetailSynopsisUiModel
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress
import hu.bbara.purefin.ui.common.permission.rememberNotificationPermissionGate
import hu.bbara.purefin.ui.screen.series.components.DownloadOptionsBottomSheet
import hu.bbara.purefin.ui.screen.series.components.EpisodeCarousel
import hu.bbara.purefin.ui.screen.series.components.SeasonTabs
import hu.bbara.purefin.ui.screen.series.components.SeriesDownloadButtonTag
import hu.bbara.purefin.ui.screen.series.components.SeriesDownloadOption
import hu.bbara.purefin.ui.screen.series.components.SeriesPlayButtonTag
import hu.bbara.purefin.ui.screen.series.components.SeriesTopBar
import hu.bbara.purefin.ui.screen.series.components.SeriesWatchedButtonTag
import hu.bbara.purefin.ui.screen.series.components.SmartDownloadCountSheet
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import java.util.UUID

@Composable
fun SeriesScreen(
    series: SeriesDto,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    LaunchedEffect(series) {
        viewModel.selectSeries(series)
    }

    val seriesState = viewModel.series.collectAsStateWithLifecycle()
    val requestNotificationPermission = rememberNotificationPermissionGate()

    val seriesData = seriesState.value
    if (seriesData != null) {
        LaunchedEffect(seriesData) {
            viewModel.observeSeriesDownloadState(seriesData)
        }
        val seriesDownloadState = viewModel.seriesDownloadState.collectAsStateWithLifecycle().value
        val seasonDownloadState = viewModel.seasonDownloadState.collectAsStateWithLifecycle().value
        val isSmartDownloadEnabled = viewModel.isSmartDownloadEnabled.collectAsStateWithLifecycle().value
        var smartDownloadCount by remember { mutableStateOf(SmartDownloadStore.DEFAULT_SMART_DOWNLOAD_COUNT) }
        val smartDownloadCountOptions = remember { listOf(1, 3, 5, 10, 15) }
        var showSmartDownloadCountSheet by remember { mutableStateOf(false) }

        fun canPerformDownloadOption(option: SeriesDownloadOption): Boolean =
            option.canPerform(
                seriesDownloadState = seriesDownloadState,
                seasonDownloadState = seasonDownloadState,
                isSmartDownloadEnabled = isSmartDownloadEnabled
            )

        fun performDownloadOption(option: SeriesDownloadOption, selectedSeason: Season) {
            when (option) {
                SeriesDownloadOption.SEASON ->
                    viewModel.downloadSeason(seriesData.id, selectedSeason.id)

                SeriesDownloadOption.SERIES ->
                    viewModel.downloadSeries(seriesData)

                SeriesDownloadOption.SMART -> {
                    showSmartDownloadCountSheet = true
                }

                SeriesDownloadOption.DELETE_SMART ->
                    viewModel.deleteSmartDownloads(seriesData.id)
            }
        }

        SeriesScreenInternal(
            series = seriesData,
            selectSeason = viewModel::selectSeason,
            seriesDownloadState = seriesDownloadState,
            seasonDownloadState = seasonDownloadState,
            isSmartDownloadEnabled = isSmartDownloadEnabled,
            onDownloadOptionSelected = { option, selectedSeason ->
                if (canPerformDownloadOption(option)) {
                    if (option.requiresNotificationPermission()) {
                        requestNotificationPermission {
                            performDownloadOption(option, selectedSeason)
                        }
                    } else {
                        performDownloadOption(option, selectedSeason)
                    }
                }
            },
            onObserveSeasonDownloadState = viewModel::observeSeasonDownloadState,
            onBack = viewModel::onGoHome,
            onMarkAsWatched = viewModel::markAsWatched,
            offline = series.offline,
            modifier = modifier
        )

        if (showSmartDownloadCountSheet) {
            SmartDownloadCountSheet(
                countOptions = smartDownloadCountOptions,
                selectedCount = smartDownloadCount,
                onCountSelected = { smartDownloadCount = it },
                onConfirm = {
                    showSmartDownloadCountSheet = false
                    requestNotificationPermission {
                        viewModel.enableSmartDownload(seriesData.id, smartDownloadCount)
                    }
                },
                onBack = { showSmartDownloadCountSheet = false },
                onDismiss = { showSmartDownloadCountSheet = false }
            )
        }
    } else {
        PurefinWaitingScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesScreenInternal(
    series: Series,
    selectSeason: (UUID, UUID) -> Unit,
    seriesDownloadState: DownloadState,
    seasonDownloadState: DownloadState,
    isSmartDownloadEnabled: Boolean,
    onDownloadOptionSelected: (SeriesDownloadOption, Season) -> Unit,
    onObserveSeasonDownloadState: (List<Episode>) -> Unit,
    onBack: () -> Unit,
    onMarkAsWatched: (Boolean) -> Unit = {},
    offline: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val navigationManager = LocalNavigationManager.current
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showMarkAsWatchedDialog by remember { mutableStateOf(false) }

    fun getDefaultSeason(): Season? {
        return series.allSeasons.firstOrNull { it.unwatchedEpisodeCount > 0 } ?: series.allSeasons.firstOrNull()
    }

    var selectedSeasonId by remember(series.id) { mutableStateOf(getDefaultSeason()?.id) }
    val selectedSeason =
        series.allSeasons.firstOrNull { it.id == selectedSeasonId } ?: getDefaultSeason()
    val nextUpEpisode = selectedSeason?.episodes?.firstOrNull { !it.watched }
        ?: selectedSeason?.episodes?.firstOrNull()
    val playAction = remember(nextUpEpisode, offline) {
        nextUpEpisode?.let { episode ->
            {
                navigationManager.navigate(
                    Route.EpisodeRoute(
                        EpisodeDto(
                            id = episode.id,
                            seasonId = episode.seasonId,
                            seriesId = episode.seriesId,
                            offline = offline,
                        )
                    )
                )
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra("MEDIA_ID", episode.id.toString())
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(series.id, selectedSeason?.id) {
        selectedSeason?.let { selectSeason(series.id, it.id) }
    }

    LaunchedEffect(selectedSeason?.id, selectedSeason?.episodes) {
        selectedSeason?.let { onObserveSeasonDownloadState(it.episodes) }
    }

    MediaDetailScaffold(
        uiModel = series.toMediaDetailScaffoldUiModel(
            selectedSeason = selectedSeason,
            nextUpEpisode = nextUpEpisode,
            onPlayClick = playAction ?: {},
            onDownloadClick = { showDownloadDialog = true },
            onMarkAsWatched = { watched ->
                if (watched) {
                    showMarkAsWatchedDialog = true
                } else {
                    onMarkAsWatched(false)
                }
            },
            bodyColor = scheme.onSurface
        ),
        modifier = modifier,
        topBar = { scrollBehavior ->
            SeriesTopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
    ) { _modifier ->
        if (selectedSeason != null) {
            SeasonTabs(
                seasons = series.allSeasons,
                selectedSeason = selectedSeason,
                onSelect = { selectedSeasonId = it.id },
                modifier = _modifier
            )
            EpisodeCarousel(
                episodes = selectedSeason.episodes,
                modifier = _modifier
            )
        } else {
            Text(
                text = "Loading seasons...",
                color = scheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = _modifier
            )
        }
    }

    if (showDownloadDialog && selectedSeason != null) {
        DownloadOptionsBottomSheet(
            selectedSeasonName = selectedSeason.name,
            seriesDownloadState = seriesDownloadState,
            seasonDownloadState = seasonDownloadState,
            isSmartDownloadEnabled = isSmartDownloadEnabled,
            onDownloadOptionSelected = {
                showDownloadDialog = false
                onDownloadOptionSelected(it, selectedSeason)
            },
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showMarkAsWatchedDialog) {
        MarkSeriesAsWatchedConfirmationDialog(
            onConfirm = {
                showMarkAsWatchedDialog = false
                onMarkAsWatched(true)
            },
            onDismiss = { showMarkAsWatchedDialog = false }
        )
    }
}

private fun Series.toMediaDetailScaffoldUiModel(
    selectedSeason: Season?,
    nextUpEpisode: Episode?,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onMarkAsWatched: (Boolean) -> Unit,
    bodyColor: Color,
): MediaDetailScaffoldUiModel = MediaDetailScaffoldUiModel(
    imageUrl = ImageUrlBuilder.finishImageUrl(imageUrlPrefix, ArtworkKind.PRIMARY),
    title = name,
    titleFontSize = 30.sp,
    titleLineHeight = 36.sp,
    metadataItems = listOf(year, "$seasonCount Seasons"),
    actions = selectedSeason?.let {
        val seriesWatched = unwatchedEpisodeCount == 0
        val playButtonText = mediaPlayButtonText(nextUpEpisode?.progress, nextUpEpisode?.watched)
        MediaDetailActionsUiModel(
            primaryAction = MediaDetailPrimaryActionUiModel(
                text = if (nextUpEpisode != null) {
                    "$playButtonText S${it.index} • E${nextUpEpisode.index}"
                } else {
                    playButtonText
                },
                progress = mediaPlaybackProgress(nextUpEpisode?.progress),
                onClick = onPlayClick,
                testTag = SeriesPlayButtonTag
            ),
            secondaryActions = listOf(
                MediaDetailSecondaryActionUiModel.MarkAsWatched(
                    watched = seriesWatched,
                    onClick = { onMarkAsWatched(!seriesWatched) },
                    testTag = SeriesWatchedButtonTag
                ),
                MediaDetailSecondaryActionUiModel.Icon(
                    icon = Icons.Outlined.Download,
                    onClick = onDownloadClick,
                    testTag = SeriesDownloadButtonTag
                )
            )
        )
    },
    synopsis = MediaDetailSynopsisUiModel(
        text = synopsis,
        bodyColor = bodyColor,
        bodyFontSize = 13.sp,
        bodyLineHeight = null,
        titleSpacing = 8.dp
    ),
    cast = if (cast.isNotEmpty()) {
        MediaDetailCastUiModel(
            members = cast,
            cardWidth = 84.dp,
            nameFontSize = 11.sp,
            roleFontSize = 10.sp
        )
    } else {
        null
    }
)

private fun SeriesDownloadOption.canPerform(
    seriesDownloadState: DownloadState,
    seasonDownloadState: DownloadState,
    isSmartDownloadEnabled: Boolean,
): Boolean = when (this) {
    SeriesDownloadOption.SEASON -> seasonDownloadState is DownloadState.NotDownloaded
    SeriesDownloadOption.SERIES -> seriesDownloadState is DownloadState.NotDownloaded
    SeriesDownloadOption.SMART -> !isSmartDownloadEnabled
    SeriesDownloadOption.DELETE_SMART -> true
}

private fun SeriesDownloadOption.requiresNotificationPermission(): Boolean = when (this) {
    SeriesDownloadOption.SEASON,
    SeriesDownloadOption.SERIES,
    SeriesDownloadOption.SMART -> true
    SeriesDownloadOption.DELETE_SMART -> false
}

@Composable
private fun MarkSeriesAsWatchedConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark series as watched?") },
        text = { Text("This will mark every episode of this series as watched.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Mark as watched")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
