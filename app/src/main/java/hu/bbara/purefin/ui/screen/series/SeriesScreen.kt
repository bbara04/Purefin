package hu.bbara.purefin.ui.screen.series

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.core.data.download.DownloadState
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.core.model.CastMember
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.content.series.SeriesViewModel
import hu.bbara.purefin.ui.screen.series.components.CastRow
import hu.bbara.purefin.ui.screen.series.components.EpisodeCarousel
import hu.bbara.purefin.ui.screen.series.components.SeasonTabs
import hu.bbara.purefin.ui.screen.series.components.SeriesActionButtons
import hu.bbara.purefin.ui.screen.series.components.SeriesDownloadOption
import hu.bbara.purefin.ui.screen.series.components.SeriesMetaChips
import hu.bbara.purefin.ui.screen.series.components.SeriesTopBar
import hu.bbara.purefin.ui.theme.AppTheme
import org.jellyfin.sdk.model.api.ImageType
import java.util.UUID

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
            seriesDownloadState = viewModel.seriesDownloadState.collectAsState().value,
            seasonDownloadState = viewModel.seasonDownloadState.collectAsState().value,
            onDownloadOptionSelected = { option, selectedSeason ->
                when (option) {
                    SeriesDownloadOption.SEASON ->
                        viewModel.downloadSeason(selectedSeason.episodes)
                    SeriesDownloadOption.SERIES ->
                        viewModel.downloadSeries(seriesData)
                    SeriesDownloadOption.SMART ->
                        viewModel.enableSmartDownload(seriesData.id)
                }
            },
            onObserveSeasonDownloadState = viewModel::observeSeasonDownloadState,
            onBack = viewModel::onGoHome,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
private fun SeriesScreenInternal(
    series: Series,
    seriesDownloadState: DownloadState,
    seasonDownloadState: DownloadState,
    onDownloadOptionSelected: (SeriesDownloadOption, Season) -> Unit,
    onObserveSeasonDownloadState: (List<Episode>) -> Unit,
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

    LaunchedEffect(selectedSeason.id, selectedSeason.episodes) {
        onObserveSeasonDownloadState(selectedSeason.episodes)
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
            SeriesHeroSection(series = series)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                SeriesActionButtons(
                    nextUpEpisode = nextUpEpisode,
                    seriesDownloadState = seriesDownloadState,
                    selectedSeason = selectedSeason,
                    seasonDownloadState = seasonDownloadState,
                    onDownloadOptionSelected = { option ->
                        onDownloadOptionSelected(option, selectedSeason)
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

@Composable
private fun SeriesHeroSection(
    series: Series,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sectionHeight = screenHeight * 0.4f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sectionHeight)
    ) {
        MediaHero(
            imageUrl = JellyfinImageHelper.finishImageUrl(series.imageUrlPrefix, ImageType.PRIMARY),
            backgroundColor = scheme.background,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            scheme.background.copy(alpha = 0.5f),
                            scheme.background
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeriesScreenPreview() {
    AppTheme {
        SeriesScreenInternal(
            series = previewSeries(),
            seriesDownloadState = DownloadState.Downloading(progressPercent = 0.58f),
            seasonDownloadState = DownloadState.NotDownloaded,
            onDownloadOptionSelected = { _, _ -> },
            onObserveSeasonDownloadState = {},
            onBack = {}
        )
    }
}

private fun previewSeries(): Series {
    val libraryId = UUID.fromString("66666666-6666-6666-6666-666666666666")
    val seriesId = UUID.fromString("77777777-7777-7777-7777-777777777777")
    val seasonOneId = UUID.fromString("88888888-8888-8888-8888-888888888888")
    val seasonTwoId = UUID.fromString("99999999-9999-9999-9999-999999999999")

    val seasonOneEpisodes = listOf(
        Episode(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1"),
            seriesId = seriesId,
            seasonId = seasonOneId,
            index = 1,
            title = "A Fresh Start",
            synopsis = "A fractured crew tries to reassemble after a year apart.",
            releaseDate = "2024",
            rating = "16+",
            runtime = "51m",
            progress = 100.0,
            watched = true,
            format = "4K",
            imageUrlPrefix = "https://images.unsplash.com/photo-1497032205916-ac775f0649ae",
            cast = emptyList()
        ),
        Episode(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2"),
            seriesId = seriesId,
            seasonId = seasonOneId,
            index = 2,
            title = "Signals",
            synopsis = "Anomalies around the station point to a cover-up.",
            releaseDate = "2024",
            rating = "16+",
            runtime = "48m",
            progress = 34.0,
            watched = false,
            format = "4K",
            imageUrlPrefix = "https://images.unsplash.com/photo-1520034475321-cbe63696469a",
            cast = emptyList()
        )
    )
    val seasonTwoEpisodes = listOf(
        Episode(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3"),
            seriesId = seriesId,
            seasonId = seasonTwoId,
            index = 1,
            title = "Return Window",
            synopsis = "A high-risk jump changes the rules of the mission.",
            releaseDate = "2025",
            rating = "16+",
            runtime = "54m",
            progress = null,
            watched = false,
            format = "4K",
            imageUrlPrefix = "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429",
            cast = emptyList()
        )
    )

    return Series(
        id = seriesId,
        libraryId = libraryId,
        name = "Constellation",
        synopsis = "When an experiment in orbit goes wrong, the survivors return home to a world that no longer fits their memories.",
        year = "2024",
        imageUrlPrefix = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa",
        unwatchedEpisodeCount = 2,
        seasonCount = 2,
        seasons = listOf(
            Season(
                id = seasonOneId,
                seriesId = seriesId,
                name = "Season 1",
                index = 1,
                unwatchedEpisodeCount = 1,
                episodeCount = seasonOneEpisodes.size,
                episodes = seasonOneEpisodes
            ),
            Season(
                id = seasonTwoId,
                seriesId = seriesId,
                name = "Season 2",
                index = 2,
                unwatchedEpisodeCount = 1,
                episodeCount = seasonTwoEpisodes.size,
                episodes = seasonTwoEpisodes
            )
        ),
        cast = listOf(
            CastMember("Noomi Rapace", "Jo", null),
            CastMember("Jonathan Banks", "Henry", null),
            CastMember("James D'Arcy", "Magnus", null)
        )
    )
}
