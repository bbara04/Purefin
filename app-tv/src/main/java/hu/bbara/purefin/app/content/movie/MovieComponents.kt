package hu.bbara.purefin.app.content.movie

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar

@Composable
internal fun MovieTopBar(
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
