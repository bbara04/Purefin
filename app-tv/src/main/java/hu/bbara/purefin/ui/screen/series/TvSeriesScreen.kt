package hu.bbara.purefin.ui.screen.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.content.series.SeriesViewModel
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.navigation.SeriesDto
import hu.bbara.purefin.ui.common.media.MediaDetailOverviewSection
import hu.bbara.purefin.ui.common.media.MediaDetailSectionTitle
import hu.bbara.purefin.ui.common.media.TvMediaDetailBodyBox
import hu.bbara.purefin.ui.common.media.TvMediaDetailScaffold
import hu.bbara.purefin.ui.common.media.tvMediaDetailBackgroundImageUrl
import hu.bbara.purefin.ui.screen.series.components.CastRow
import hu.bbara.purefin.ui.screen.series.components.SeriesFirstSeasonTabTag
import hu.bbara.purefin.ui.screen.series.components.TvEpisodeCarousel
import hu.bbara.purefin.ui.screen.series.components.TvSeasonTabs
import hu.bbara.purefin.ui.screen.series.components.TvSeriesHeroSection
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import java.util.UUID

@Composable
fun TvSeriesScreen(
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
        TvSeriesScreenContent(
            series = seriesData,
            onPlayEpisode = viewModel::onPlayEpisode,
            modifier = modifier
        )
    } else {
        PurefinWaitingScreen()
    }
}

@Composable
internal fun TvSeriesScreenContent(
    series: Series,
    onPlayEpisode: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSeason by remember(series.id) { mutableStateOf(series.defaultSeason()) }
    val nextUpEpisode = remember(series.id) { series.nextUpEpisode() }
    val playFocusRequester = remember { FocusRequester() }
    val firstContentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(series.id, nextUpEpisode?.id) {
        withFrameNanos { }
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
        item(key = "series-hero") {
            TvMediaDetailBodyBox(
                backgroundImageUrl = tvMediaDetailBackgroundImageUrl(series.imageUrlPrefix),
                modifier = it
            ) {
                TvSeriesHeroSection(
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
        item(key = "series-overview") {
            Column(modifier = it.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaDetailOverviewSection(
                    synopsis = series.synopsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        item(key = "series-season-tabs") {
            TvSeasonTabs(
                seasons = series.seasons,
                selectedSeason = selectedSeason,
                firstItemFocusRequester = firstContentFocusRequester,
                firstItemTestTag = SeriesFirstSeasonTabTag,
                onSelect = { selectedSeason = it },
                modifier = it
            )
        }
        item(key = "series-episodes") {
            Spacer(modifier = Modifier.height(20.dp))
            TvEpisodeCarousel(
                episodes = selectedSeason.episodes,
                modifier = it
            )
        }
        if (series.cast.isNotEmpty()) {
            item(key = "series-cast") {
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
