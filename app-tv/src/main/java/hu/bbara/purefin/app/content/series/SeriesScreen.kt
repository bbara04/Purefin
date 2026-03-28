package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        SeriesScreenInternal(
            series = seriesData,
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val textMutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val hPad = Modifier.padding(horizontal = 16.dp)

    fun getDefaultSeason(): Season {
        for (season in series.seasons) {
            val firstUnwatchedEpisode = season.episodes.firstOrNull { it.watched.not() }
            if (firstUnwatchedEpisode != null) return season
        }
        return series.seasons.first()
    }
    val selectedSeason = remember { mutableStateOf<Season>(getDefaultSeason()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        item {
            Box {
                MediaHero(
                    imageUrl = series.heroImageUrl,
                    heightFraction = 0.30f,
                    backgroundColor = scheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
                SeriesTopBar(onBack = onBack)
            }
        }
        item {
            Column(modifier = hPad) {
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
        item {
            Spacer(modifier = Modifier.height(24.dp))
            MediaSynopsis(
                synopsis = series.synopsis,
                bodyColor = textMutedStrong,
                bodyFontSize = 13.sp,
                bodyLineHeight = null,
                titleSpacing = 8.dp,
                modifier = hPad
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SeasonTabs(
                seasons = series.seasons,
                selectedSeason = selectedSeason.value,
                onSelect = { selectedSeason.value = it },
                modifier = hPad
            )
        }
        item {
            EpisodeCarousel(
                episodes = selectedSeason.value.episodes,
                modifier = hPad
            )
        }
        if (series.cast.isNotEmpty()) {
            item {
                Column(modifier = hPad) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cast",
                        color = scheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CastRow(cast = series.cast)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
