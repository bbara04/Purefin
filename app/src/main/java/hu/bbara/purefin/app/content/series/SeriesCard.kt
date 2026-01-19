package hu.bbara.purefin.app.content.series

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeriesCard(
    series: SeriesUiModel,
    modifier: Modifier = Modifier,
) {

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
                imageUrl = series.heroImageUrl,
                height = heroHeight
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-96).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = series.title,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SeriesMetaChips(series = series)
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
                        text = series.synopsis,
                        color = SeriesMutedStrong,
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = "Episodes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    SeasonTabs(seasons = series.seasonTabs)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                EpisodeCarousel(
                    episodes = series.seasonTabs.firstOrNull { it.isSelected }?.episodes
                        ?: series.seasonTabs.firstOrNull()?.episodes
                        ?: emptyList()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = "Cast",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CastRow(cast = series.cast)
                }
            }
        }

        SeriesTopBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Preview
@Composable
fun SeriesCardPreview() {
    SeriesCard(series = SeriesMockData.series())
}
