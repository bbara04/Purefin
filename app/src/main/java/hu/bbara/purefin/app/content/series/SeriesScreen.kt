package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.feature.shared.content.series.SeriesViewModel

@Composable
fun SeriesScreen(
    series: SeriesDto,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    LaunchedEffect(series.id) {
        viewModel.selectSeries(series.id)
    }

    val series = viewModel.series.collectAsState()

    val seriesData = series.value
    if (seriesData != null && seriesData.seasons.isNotEmpty()) {
        LaunchedEffect(seriesData) {
            viewModel.observeSeriesDownloadState(seriesData)
        }
        SeriesScreenInternal(
            series = seriesData,
            viewModel = viewModel,
            onBack = viewModel::onBack,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
private fun SeriesScreenInternal(
    series: Series,
    viewModel: SeriesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val textMutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)

    fun getDefaultSeason() : Season {
        for (season in series.seasons) {
            val firstUnwatchedEpisode = season.episodes.firstOrNull {
                it.watched.not()
            }
            if (firstUnwatchedEpisode != null) {
                return season
            }
        }
        return series.seasons.first()
    }
    var selectedSeasonId by remember(series.id) { mutableStateOf(getDefaultSeason().id) }
    val selectedSeason = series.seasons.firstOrNull { it.id == selectedSeasonId } ?: getDefaultSeason()
    val nextUpEpisode = remember(series) {
        series.seasons.firstNotNullOfOrNull { season ->
            season.episodes.firstOrNull { !it.watched }
        } ?: series.seasons.firstOrNull()?.episodes?.firstOrNull()
    }

    val seriesDownloadState by viewModel.seriesDownloadState.collectAsState()
    val seasonDownloadState by viewModel.seasonDownloadState.collectAsState()
    LaunchedEffect(selectedSeason.id, selectedSeason.episodes) {
        viewModel.observeSeasonDownloadState(selectedSeason.episodes)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SeriesTopBar(
                onBack = onBack,
                modifier = Modifier
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MediaHero(
                imageUrl = series.heroImageUrl,
                heightFraction = 0.30f,
                backgroundColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Text(
                    text = series.name,
                    color = scheme.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                SeriesMetaChips(series = series)
                Spacer(modifier = Modifier.height(24.dp))
                SeriesActionButtons(
                    nextUpEpisode = nextUpEpisode,
                    seriesDownloadState = seriesDownloadState,
                    selectedSeason = selectedSeason,
                    seasonDownloadState = seasonDownloadState,
                    onDownloadOptionSelected = { option ->
                        when (option) {
                            SeriesDownloadOption.SEASON ->
                                viewModel.downloadSeason(selectedSeason.episodes)
                            SeriesDownloadOption.SERIES ->
                                viewModel.downloadSeries(series)
                            SeriesDownloadOption.SMART ->
                                viewModel.enableSmartDownload(series.id)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                MediaSynopsis(
                    synopsis = series.synopsis,
                    bodyColor = textMutedStrong,
                    bodyFontSize = 13.sp,
                    bodyLineHeight = null,
                    titleSpacing = 8.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SeasonTabs(
                    seasons = series.seasons,
                    selectedSeason = selectedSeason,
                    onSelect = { selectedSeasonId = it.id }
                )
                EpisodeCarousel(
                    episodes = selectedSeason.episodes,
                )
                Spacer(modifier = Modifier.height(16.dp))
                if(series.cast.isNotEmpty()) {
                    Text(
                        text = "Cast",
                        color = scheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CastRow(cast = series.cast)
                }
            }
        }
    }
}
