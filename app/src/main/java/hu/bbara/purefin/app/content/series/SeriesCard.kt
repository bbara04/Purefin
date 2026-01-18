package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.navigation.ItemDto

@Composable
fun SeriesCard(
    series: ItemDto,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    LaunchedEffect(series.id) {
        viewModel.selectSeries(series.id)
    }

    val series = viewModel.series.collectAsState()

    if (series.value != null) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(SeriesBackgroundDark)
        ) {
            val heroHeight = maxHeight * 0.6f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                SeriesHero(
                    imageUrl = series.value!!.heroImageUrl,
                    height = heroHeight
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-96).dp)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = series.value!!.title,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SeriesMetaChips(series = series.value!!)
                    Spacer(modifier = Modifier.height(24.dp))
                    SeriesActionButtons()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Synopsis",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = series.value!!.synopsis,
                        color = SeriesMutedStrong,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    SeasonTabs(seasons = series.value!!.seasonTabs)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                EpisodeCarousel(episodes = series.value!!.seasonTabs.firstOrNull { it.isSelected }?.episodes.orEmpty())
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = "Cast",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CastRow(cast = series.value!!.cast)
                }
            }

            SeriesTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.TopCenter)
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SeriesBackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading...",
                color = Color.White
            )
        }
    }
}
