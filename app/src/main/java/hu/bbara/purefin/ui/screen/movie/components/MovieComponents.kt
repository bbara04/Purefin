package hu.bbara.purefin.ui.screen.movie.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MovieTopBar(
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    DefaultTopBar(
        leftActions = {
            GhostIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
        },
        rightActions = {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            GhostIconButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "More",
                onClick = { })
        },
        withIcon = false,
        scrollBehavior = scrollBehavior
    )
}

internal const val MoviePlayButtonTag = "movie-play-button"
internal const val MovieDownloadButtonTag = "movie-download-button"
