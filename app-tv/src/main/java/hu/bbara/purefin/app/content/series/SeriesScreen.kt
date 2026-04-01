package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaDetailOverviewSection
import hu.bbara.purefin.common.ui.components.MediaDetailSectionTitle
import hu.bbara.purefin.common.ui.components.TvMediaDetailScaffold
import hu.bbara.purefin.core.data.image.JellyfinImageHelper
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.core.model.Season
import hu.bbara.purefin.core.model.Series
import hu.bbara.purefin.feature.shared.content.series.SeriesViewModel
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.ImageType

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
            onBack = viewModel::onBack,
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
    onBack: () -> Unit,
    onPlayEpisode: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun getDefaultSeason(): Season {
        for (season in series.seasons) {
            val firstUnwatchedEpisode = season.episodes.firstOrNull { it.watched.not() }
            if (firstUnwatchedEpisode != null) return season
        }
        return series.seasons.first()
    }
    val selectedSeason = remember(series.id) { mutableStateOf(getDefaultSeason()) }
    val nextUpEpisode = remember(series.id) {
        series.seasons.firstNotNullOfOrNull { season ->
            season.episodes.firstOrNull { !it.watched }
        } ?: series.seasons.firstOrNull()?.episodes?.firstOrNull()
    }
    val backFocusRequester = remember { FocusRequester() }
    val playFocusRequester = remember { FocusRequester() }
    val firstContentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(series.id) {
        backFocusRequester.requestFocus()
    }

    TvMediaDetailScaffold(
        heroImageUrl = JellyfinImageHelper.finishImageUrl(series.imageUrlPrefix, ImageType.PRIMARY),
        resetScrollKey = series.id,
        modifier = modifier,
        topBar = {
            SeriesTopBar(
                onBack = onBack,
                backFocusRequester = backFocusRequester,
                downFocusRequester = nextUpEpisode?.let { playFocusRequester } ?: firstContentFocusRequester,
                modifier = Modifier.align(Alignment.TopStart)
            )
        },
        heroContent = {
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
    ) {
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
                selectedSeason = selectedSeason.value,
                firstItemFocusRequester = firstContentFocusRequester,
                firstItemTestTag = SeriesFirstSeasonTabTag,
                onSelect = { selectedSeason.value = it },
                modifier = it
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
            EpisodeCarousel(
                episodes = selectedSeason.value.episodes,
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
