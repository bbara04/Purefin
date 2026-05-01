package hu.bbara.purefin.ui.screen.episode.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.download.DownloadState
import hu.bbara.purefin.model.Episode
import hu.bbara.purefin.player.PlayerActivity
import hu.bbara.purefin.ui.common.button.GhostIconButton
import hu.bbara.purefin.ui.common.button.MediaActionButton
import hu.bbara.purefin.ui.common.button.MediaResumeButton
import hu.bbara.purefin.ui.common.media.MediaPlaybackSettings
import hu.bbara.purefin.ui.common.media.MediaSynopsis
import hu.bbara.purefin.ui.common.media.mediaPlayButtonText
import hu.bbara.purefin.ui.common.media.mediaPlaybackProgress

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
    onSeriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GhostIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            when {
                shortcut != null -> {
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clickable(onClick = shortcut.onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortcut.label,
                            color = scheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(scheme.background.copy(alpha = 0.65f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
        }
    }
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
        Spacer(modifier = Modifier.height(24.dp))

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
        Spacer(modifier = Modifier.height(24.dp))

        MediaPlaybackSettings(
            backgroundColor = MaterialTheme.colorScheme.surface,
            foregroundColor = MaterialTheme.colorScheme.onSurface,
            //TODO fix it
            audioTrack = "ENG",
            subtitles = "ENG"
        )
        Spacer(modifier = Modifier.height(24.dp))

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
