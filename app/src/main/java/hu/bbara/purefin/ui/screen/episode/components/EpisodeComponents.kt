package hu.bbara.purefin.ui.screen.episode.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

internal sealed interface EpisodeTopBarShortcut {
    val label: String
    val onClick: () -> Unit

    data class Series(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Series"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeTopBar(
    shortcut: EpisodeTopBarShortcut?,
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
            when (shortcut) {
                is EpisodeTopBarShortcut.Series -> {
                    TextButton(
                        onClick = shortcut.onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag(EpisodeSeriesButtonTag)
                            .clip(CircleShape)
                    ) {
                        Text(
                            text = shortcut.label,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                null -> {}
            }
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

internal const val EpisodeSeriesButtonTag = "episode-series-button"
internal const val EpisodePlayButtonTag = "episode-play-button"
internal const val EpisodeDownloadButtonTag = "episode-download-button"
internal const val EpisodeWatchedButtonTag = "episode-watched-button"
