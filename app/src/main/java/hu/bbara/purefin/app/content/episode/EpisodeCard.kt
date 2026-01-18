package hu.bbara.purefin.app.content.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.navigation.ItemDto
import org.jellyfin.sdk.model.api.BaseItemKind

@Composable
fun EpisodeCard(
    item: ItemDto,
    modifier: Modifier = Modifier,
    viewModel: EpisodeScreenViewModel = hiltViewModel()
) {

    LaunchedEffect(item) {
        when (item.type) {
            BaseItemKind.EPISODE -> viewModel.selectEpisode(item.id)
            else -> return@LaunchedEffect
        }
    }

    val episodeItem = viewModel.episode.collectAsState()

    if (episodeItem.value != null) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(EpisodeBackgroundDark)
        ) {
            val isWide = maxWidth >= 900.dp
            val contentPadding = if (isWide) 32.dp else 20.dp

            Box(modifier = Modifier.fillMaxSize()) {
                if (isWide) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        EpisodeHero(
                            episode = episodeItem.value!!,
                            height = 300.dp,
                            isWide = true,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.5f)
                        )
                        EpisodeDetails(
                            episode = episodeItem.value!!,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(start = contentPadding, end = contentPadding, top = 96.dp, bottom = 32.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EpisodeHero(
                            episode = episodeItem.value!!,
                            height = 400.dp,
                            isWide = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        EpisodeDetails(
                            episode = episodeItem.value!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = contentPadding)
                                .offset(y = (-48).dp)
                                .padding(bottom = 96.dp)
                        )
                    }
                }

                EpisodeTopBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )

                if (!isWide) {
                    FloatingPlayButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(EpisodeBackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading...",
                color = Color.White
            )
        }
    }
}
