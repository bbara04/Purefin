package hu.bbara.purefin.ui.screen.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    LaunchedEffect(series) {
        viewModel.selectSeries(series)
    }

    val series = viewModel.series.collectAsStateWithLifecycle()

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
    val initialFocusSeasonId = remember(series.id, focusedSeasonId) { defaultSeason.id }
    val initialFocusSeason = series.seasons.firstOrNull { it.id == initialFocusSeasonId } ?: defaultSeason
    val initialFocusedEpisodeId = initialFocusSeason.focusTargetEpisodeId(focusedEpisodeId)
    val seasonTabFocusRequester = remember { FocusRequester() }
    var requestedInitialEpisodeFocus by remember(series.id, focusedSeasonId, focusedEpisodeId) {
        mutableStateOf(false)
    }
    val waitingForInitialEpisodes = initialFocusedEpisodeId == null &&
        initialFocusSeason.episodes.isEmpty() &&
        initialFocusSeason.episodeCount > 0

    LaunchedEffect(series.id, selectedSeason.id) {
        onLoadSeasonEpisodes(series.id, selectedSeason.id)
    }

    LaunchedEffect(series.id, initialFocusedEpisodeId, waitingForInitialEpisodes) {
        if (initialFocusedEpisodeId != null || waitingForInitialEpisodes) return@LaunchedEffect
        withFrameNanos { }
        seasonTabFocusRequester.requestFocus()
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
                    selectedItemFocusRequester = seasonTabFocusRequester,
                    firstItemTestTag = SeriesFirstSeasonTabTag,
                    onSelect = { selectedSeasonId = it.id },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TvEpisodeCarousel(
                    episodes = selectedSeason.episodes,
                    onPlayEpisode = { onPlayEpisode(it.id) },
                    focusedEpisodeId = initialFocusedEpisodeId,
                    requestFocus = selectedSeason.id == initialFocusSeasonId && !requestedInitialEpisodeFocus,
                    onFocusRequested = { requestedInitialEpisodeFocus = true },
                    upFocusRequester = seasonTabFocusRequester,
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

private fun Season.focusTargetEpisodeId(focusedEpisodeId: UUID?): UUID? {
    val focusedEpisode = episodes.firstOrNull { it.id == focusedEpisodeId }
    return focusedEpisode?.id ?: nextUpEpisode()?.id
}
