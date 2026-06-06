package hu.bbara.purefin.ui.screen.series

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.core.feature.content.series.SeriesViewModel
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.ui.common.media.MediaDetailScaffold
import hu.bbara.purefin.ui.common.media.MediaSynopsis
import hu.bbara.purefin.ui.common.permission.rememberNotificationPermissionGate
import hu.bbara.purefin.ui.screen.series.components.CastRow
import hu.bbara.purefin.ui.screen.series.components.EpisodeCarousel
import hu.bbara.purefin.ui.screen.series.components.SeasonTabs
import hu.bbara.purefin.ui.screen.series.components.SeriesActionButtons
import hu.bbara.purefin.ui.screen.series.components.SeriesDownloadOption
import hu.bbara.purefin.ui.screen.series.components.SeriesMetaChips
import hu.bbara.purefin.ui.screen.series.components.SeriesTopBar
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

                SeriesDownloadOption.SMART ->
                    viewModel.enableSmartDownload(seriesData.id)

                SeriesDownloadOption.DELETE_SMART ->
                    viewModel.deleteSmartDownloads(seriesData.id)
            }
        }

        SeriesScreenInternal(
            series = seriesData,
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
            onLoadSeasonEpisodes = viewModel::loadSeasonEpisodes,
            onObserveSeasonDownloadState = viewModel::observeSeasonDownloadState,
            onBack = viewModel::onGoHome,
            offline = series.offline,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesScreenInternal(
    series: Series,
    seriesDownloadState: DownloadState,
    seasonDownloadState: DownloadState,
    isSmartDownloadEnabled: Boolean,
    onDownloadOptionSelected: (SeriesDownloadOption, Season) -> Unit,
    onLoadSeasonEpisodes: (UUID, UUID) -> Unit,
    onObserveSeasonDownloadState: (List<Episode>) -> Unit,
    onBack: () -> Unit,
    offline: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    fun getDefaultSeason(): Season? {
        return series.seasons.firstOrNull { it.unwatchedEpisodeCount > 0 } ?: series.seasons.firstOrNull()
    }

    var selectedSeasonId by remember(series.id) { mutableStateOf(getDefaultSeason()?.id) }
    val selectedSeason =
        series.seasons.firstOrNull { it.id == selectedSeasonId } ?: getDefaultSeason()
    val nextUpEpisode = selectedSeason?.episodes?.firstOrNull { !it.watched }
        ?: selectedSeason?.episodes?.firstOrNull()

    LaunchedEffect(series.id, selectedSeason?.id) {
        selectedSeason?.let { onLoadSeasonEpisodes(series.id, it.id) }
    }

    LaunchedEffect(selectedSeason?.id, selectedSeason?.episodes) {
        selectedSeason?.let { onObserveSeasonDownloadState(it.episodes) }
    }

    MediaDetailScaffold(
        imageUrl = ImageUrlBuilder.finishImageUrl(series.imageUrlPrefix, ArtworkKind.PRIMARY),
        modifier = modifier,
        topBar = { scrollBehavior ->
            SeriesTopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        },
        heroContent = { _modifier ->
            SeriesHeroContent(
                series = series,
                modifier = _modifier
            )
        }
    ) { _modifier ->
        if (selectedSeason != null) {
            SeriesActionButtons(
                nextUpEpisode = nextUpEpisode,
                selectedSeason = selectedSeason,
                seriesDownloadState = seriesDownloadState,
                seasonDownloadState = seasonDownloadState,
                isSmartDownloadEnabled = isSmartDownloadEnabled,
                offline = offline,
                onDownloadOptionSelected = { option ->
                    onDownloadOptionSelected(option, selectedSeason)
                },
                modifier = _modifier
            )
        }
        MediaSynopsis(
            synopsis = series.synopsis,
            bodyColor = scheme.onSurface,
            bodyFontSize = 13.sp,
            bodyLineHeight = null,
            titleSpacing = 8.dp,
            modifier = _modifier
        )
        if (selectedSeason != null) {
            SeasonTabs(
                seasons = series.seasons,
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
        if (series.cast.isNotEmpty()) {
            Text(
                text = "Cast",
                color = scheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = _modifier
            )
            Spacer(modifier = _modifier.height(12.dp))
            CastRow(cast = series.cast)
        }
    }
}

@Composable
private fun SeriesHeroContent(
    series: Series,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Text(
        text = series.name,
        color = scheme.onBackground,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        modifier = modifier
    )
    SeriesMetaChips(
        series = series,
        modifier = modifier
    )
}

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