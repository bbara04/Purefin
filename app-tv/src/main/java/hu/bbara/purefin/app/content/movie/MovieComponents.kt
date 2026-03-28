package hu.bbara.purefin.app.content.movie

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar

@Composable
internal fun MovieTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    MediaDetailsTopBar(
        onBack = onBack,
        modifier = modifier
    )
}
