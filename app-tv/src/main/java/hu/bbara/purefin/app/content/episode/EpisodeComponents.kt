package hu.bbara.purefin.app.content.episode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar

@Composable
internal fun EpisodeTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    downFocusRequester: FocusRequester? = null
) {
    MediaDetailsTopBar(
        onBack = onBack,
        modifier = modifier,
        downFocusRequester = downFocusRequester
    )
}
