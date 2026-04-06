package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaDetailOverviewSection
import hu.bbara.purefin.common.ui.components.MediaDetailSectionTitle
import hu.bbara.purefin.common.ui.components.TvMediaDetailBodyBox
import hu.bbara.purefin.common.ui.components.TvMediaDetailScaffold
import hu.bbara.purefin.common.ui.components.tvMediaDetailBackgroundImageUrl
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.content.series.SeriesViewModel
import org.jellyfin.sdk.model.UUID

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
        SeriesScreenContent(
            series = seriesData,
            onPlayEpisode = viewModel::onPlayEpisode,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
internal fun SeriesScreenContent(
    series: Series,
    onPlayEpisode: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSeason by remember(series.id) { mutableStateOf(series.defaultSeason()) }
    val nextUpEpisode = remember(series.id) { series.nextUpEpisode() }
    val playFocusRequester = remember { FocusRequester() }
    val firstContentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(series.id, nextUpEpisode?.id) {
        if (nextUpEpisode != null) {
            playFocusRequester.requestFocus()
        } else {
            firstContentFocusRequester.requestFocus()
        }
    }

    TvMediaDetailScaffold(
        resetScrollKey = series.id,
        modifier = modifier
    ) {
        item {
            TvMediaDetailBodyBox(
                backgroundImageUrl = tvMediaDetailBackgroundImageUrl(series.imageUrlPrefix),
                modifier = it
            ) {
                SeriesHeroSection(
                    series = series,
                    nextUpEpisode = nextUpEpisode,
                    onPlayEpisode = { onPlayEpisode(it.id) },
                    playFocusRequester = playFocusRequester,
                    firstContentFocusRequester = firstContentFocusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item {
            Column(modifier = it.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaDetailOverviewSection(
                    synopsis = series.synopsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        item {
            SeasonTabs(
                seasons = series.seasons,
                selectedSeason = selectedSeason,
                firstItemFocusRequester = firstContentFocusRequester,
                firstItemTestTag = SeriesFirstSeasonTabTag,
                onSelect = { selectedSeason = it },
                modifier = it
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            EpisodeCarousel(
                episodes = selectedSeason.episodes,
                modifier = it
            )
        }
        if (series.cast.isNotEmpty()) {
            item {
                Column(modifier = it) {
                    Spacer(modifier = Modifier.height(20.dp))
                    MediaDetailSectionTitle(text = "Cast")
                    Spacer(modifier = Modifier.height(14.dp))
                    CastRow(cast = series.cast)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun Series.defaultSeason(): Season {
    for (season in seasons) {
        if (season.episodes.any { !it.watched }) {
            return season
        }
    }
    return seasons.first()
}

private fun Series.nextUpEpisode() = seasons.firstNotNullOfOrNull { season ->
    season.episodes.firstOrNull { !it.watched }
} ?: seasons.firstOrNull()?.episodes?.firstOrNull()
