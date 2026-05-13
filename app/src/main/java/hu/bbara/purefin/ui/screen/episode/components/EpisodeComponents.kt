package hu.bbara.purefin.ui.screen.episode.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.core.download.DownloadState
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.player.PlayerActivity
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.common.button.MediaActionButton
import hu.bbara.purefin.ui.common.button.MediaResumeButton
import hu.bbara.purefin.ui.common.media.MediaPlaybackSettings
import hu.bbara.purefin.ui.common.media.MediaSynopsis
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar

internal sealed interface EpisodeTopBarShortcut {
    val label: String
    val onClick: () -> Unit

    data class Series(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Series"
    }
}

@Composable
internal fun EpisodeTopBar(
    shortcut: EpisodeTopBarShortcut?,
    onBack: () -> Unit,
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
        withIcon = false
    )
}

@Composable
internal fun EpisodeDetails(
    episode: Episode,
    downloadState: DownloadState,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val playAction = remember(episode.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", episode.id.toString())
            context.startActivity(intent)
        }
    }

    Column(modifier = modifier) {
        MediaSynopsis(
            synopsis = episode.synopsis
        )
        Row() {
            MediaResumeButton(
                text = mediaPlayButtonText(episode.progress, episode.watched),
                progress = mediaPlaybackProgress(episode.progress),
                onClick = playAction,
                modifier = Modifier.sizeIn(maxWidth = 200.dp)
            )
            VerticalDivider(
                color = MaterialTheme.colorScheme.secondary,
                thickness = 2.dp,
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row() {
                MediaActionButton(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    iconColor = MaterialTheme.colorScheme.onSurface,
                    icon = when (downloadState) {
                        is DownloadState.NotDownloaded -> Icons.Outlined.Download
                        is DownloadState.Downloading -> Icons.Outlined.Close
                        is DownloadState.Downloaded -> Icons.Outlined.DownloadDone
                        is DownloadState.Failed -> Icons.Outlined.Download
                    },
                    height = 48.dp,
                    onClick = onDownloadClick
                )
            }
        }
        MediaPlaybackSettings(
            backgroundColor = MaterialTheme.colorScheme.surface,
            foregroundColor = MaterialTheme.colorScheme.onSurface,
            //TODO fix it
            audioTrack = "ENG",
            subtitles = "ENG"
        )
        if (episode.cast.isNotEmpty()) {
            Text(
                text = "Cast",
                color = scheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            //TODO use it
//            MediaCastRow(
//                cast = episode.cast
//            )
        }
    }
}
