package hu.bbara.purefin.app.content.episode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBar
import hu.bbara.purefin.common.ui.components.MediaDetailsTopBarShortcut
import hu.bbara.purefin.core.data.navigation.Route

internal sealed interface EpisodeTopBarShortcut {
    val label: String
    val onClick: () -> Unit

    data class Series(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Series"
    }
}

internal fun episodeTopBarShortcut(
    previousRoute: Route?,
    onSeriesClick: () -> Unit
): EpisodeTopBarShortcut? {
    return when (previousRoute) {
        Route.Home -> EpisodeTopBarShortcut.Series(onClick = onSeriesClick)
        else -> null
    }
}

@Composable
internal fun EpisodeTopBar(
    onBack: () -> Unit,
    shortcut: EpisodeTopBarShortcut? = null,
    modifier: Modifier = Modifier,
    downFocusRequester: FocusRequester? = null
) {
    MediaDetailsTopBar(
        onBack = onBack,
        shortcut = shortcut?.let { MediaDetailsTopBarShortcut(label = it.label, onClick = it.onClick) },
        modifier = modifier,
        downFocusRequester = downFocusRequester
    )
}
