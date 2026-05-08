package hu.bbara.purefin.ui.screen.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import hu.bbara.purefin.core.feature.content.series.SeriesViewModel
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.model.Season
import hu.bbara.purefin.model.Series
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.ui.common.media.MediaDetailHorizontalPadding
import hu.bbara.purefin.ui.common.media.TvMediaDetailBodyBox
import hu.bbara.purefin.ui.common.media.TvMediaDetailScaffold
import hu.bbara.purefin.ui.common.media.tvMediaDetailBackgroundImageUrl
import hu.bbara.purefin.ui.screen.series.components.SeriesFirstSeasonTabTag
import hu.bbara.purefin.ui.screen.series.components.TvEpisodeCarousel
import hu.bbara.purefin.ui.screen.series.components.TvSeasonTabs
import hu.bbara.purefin.ui.screen.series.components.TvSeriesHeroSection
import hu.bbara.purefin.ui.screen.waiting.PurefinWaitingScreen
import java.util.UUID

@Composable
fun TvSeriesScreen(
    series: SeriesDto,
    focusedSeasonId: UUID? = null,
    focusedEpisodeId: UUID? = null,
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
            onLoadSeasonEpisodes = viewModel::loadSeasonEpisodes,
            focusedSeasonId = focusedSeasonId,
            focusedEpisodeId = focusedEpisodeId,
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
    onLoadSeasonEpisodes: (UUID, UUID) -> Unit = { _, _ -> },
    focusedSeasonId: UUID? = null,
    focusedEpisodeId: UUID? = null,
    modifier: Modifier = Modifier,
) {
    val defaultSeason = series.defaultSeason(focusedSeasonId)
    var selectedSeasonId by remember(series.id, focusedSeasonId) {
        mutableStateOf(defaultSeason.id)
    }
    val selectedSeason = series.seasons.firstOrNull { it.id == selectedSeasonId } ?: defaultSeason
    val initialFocusedEpisodeId = remember(series.id, focusedSeasonId, focusedEpisodeId) {
        val focusedEpisode = defaultSeason.episodes.firstOrNull { it.id == focusedEpisodeId }
        focusedEpisode?.id ?: defaultSeason.nextUpEpisode()?.id
    }
    val firstContentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(series.id, selectedSeason.id) {
        onLoadSeasonEpisodes(series.id, selectedSeason.id)
    }

    LaunchedEffect(series.id, initialFocusedEpisodeId) {
        if (initialFocusedEpisodeId != null) return@LaunchedEffect
        withFrameNanos { }
        firstContentFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        resetScrollKey = series.id,
        modifier = modifier
    ) {
        TvMediaDetailBodyBox(
            backgroundImageUrl = tvMediaDetailBackgroundImageUrl(series.imageUrlPrefix),
            modifier = Modifier.fillMaxSize(),
            heightFraction = 1f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MediaDetailHorizontalPadding)
            ) {
                TvSeriesHeroSection(
                    series = series,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TvSeasonTabs(
                    seasons = series.seasons,
                    selectedSeason = selectedSeason,
                    firstItemFocusRequester = firstContentFocusRequester,
                    firstItemTestTag = SeriesFirstSeasonTabTag,
                    onSelect = { selectedSeasonId = it.id },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TvEpisodeCarousel(
                    episodes = selectedSeason.episodes,
                    onPlayEpisode = { onPlayEpisode(it.id) },
                    focusedEpisodeId = initialFocusedEpisodeId,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun Series.defaultSeason(focusedSeasonId: UUID?): Season {
    if (focusedSeasonId != null) {
        seasons.firstOrNull { it.id == focusedSeasonId }?.let { return it }
    }

    return seasons.firstOrNull { it.unwatchedEpisodeCount > 0 } ?: seasons.first()
}

private fun Season.nextUpEpisode(): Episode? {
    return episodes.firstOrNull { !it.watched } ?: episodes.firstOrNull()
}
