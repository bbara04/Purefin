package hu.bbara.purefin.app.content.episode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar

@Composable
internal fun EpisodeTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    MediaDetailsTopBar(
        onBack = onBack,
        modifier = modifier
    )
}
