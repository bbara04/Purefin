package hu.bbara.purefin.app.content.episode

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.common.ui.PurefinWaitingScreen
import hu.bbara.purefin.navigation.ItemDto

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

    if (episode.value != null) {
        EpisodeCard(
            episode = episode.value!!,
            modifier = modifier,
            backGroundColor = MaterialTheme.colorScheme.background
        )
    } else {
        PurefinWaitingScreen()
    }
}
