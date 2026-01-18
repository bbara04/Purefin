package hu.bbara.purefin.app.content.episode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.navigation.ItemDto

@Composable
fun EpisodeScreen(
    episode: ItemDto,
    modifier: Modifier = Modifier
) {
    EpisodeCard(
        item = episode,
        modifier = modifier
    )
}
