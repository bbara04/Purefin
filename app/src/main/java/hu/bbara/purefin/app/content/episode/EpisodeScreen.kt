package hu.bbara.purefin.app.content.episode

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.app.content.ContentMockData
import hu.bbara.purefin.common.ui.MediaFloatingPlayButton
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.common.ui.components.MediaHero
import hu.bbara.purefin.common.ui.toMediaDetailColors
import hu.bbara.purefin.navigation.ItemDto
import hu.bbara.purefin.player.PlayerActivity

@Composable
fun EpisodeScreen(
    episode: ItemDto,
    viewModel: EpisodeScreenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    LaunchedEffect(episode) {
        viewModel.selectEpisode(episode.id)
    }

    val episode = viewModel.episode.collectAsState()

    if (episode.value == null) {
        PurefinWaitingScreen()
        return
    }

    EpisodeScreenInternal(
    episode = episode.value!!,
        modifier = modifier,
        backGroundColor = MaterialTheme.colorScheme.background
    )
}

@Composable
private fun EpisodeScreenInternal(
    episode: EpisodeUiModel,
    backGroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = rememberEpisodeColors().toMediaDetailColors()
    val context = LocalContext.current
    val playAction = remember(episode.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", episode.id.toString())
            context.startActivity(intent)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val isWide = maxWidth >= 900.dp
        val contentPadding = if (isWide) 32.dp else 20.dp

        Box(modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    MediaHero(
                        imageUrl = episode.heroImageUrl,
                        height = 300.dp,
                        backgroundColor = backGroundColor,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                    )
                    EpisodeDetails(
                        episode = episode,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = contentPadding,
                                end = contentPadding,
                                top = 96.dp,
                                bottom = 32.dp
                            )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    MediaHero(
                        imageUrl = episode.heroImageUrl,
                        backgroundColor = backGroundColor,
                        height = 400.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    EpisodeDetails(
                        episode = episode,
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
                MediaFloatingPlayButton(
                    containerColor = colors.primary,
                    onContainerColor = colors.onPrimary,
                    onClick = playAction,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun EpisodeScreenPreview() {
    EpisodeScreenInternal(
        episode = ContentMockData.episode(),
        backGroundColor = MaterialTheme.colorScheme.background
    )
}
