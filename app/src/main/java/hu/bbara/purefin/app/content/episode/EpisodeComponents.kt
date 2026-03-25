package hu.bbara.purefin.app.content.episode

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.components.GhostIconButton
import hu.bbara.purefin.common.ui.components.MediaActionButton
import hu.bbara.purefin.common.ui.components.MediaPlaybackSettings
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.core.model.Episode
import hu.bbara.purefin.feature.download.DownloadState
import hu.bbara.purefin.player.PlayerActivity

internal sealed interface EpisodeTopBarShortcut {
    val label: String
    val onClick: () -> Unit

    data class Series(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Series"
    }

    data class Home(override val onClick: () -> Unit) : EpisodeTopBarShortcut {
        override val label: String = "Home"
    }
}

@Composable
internal fun EpisodeTopBar(
    seriesTitle: String?,
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
            !seriesTitle.isNullOrBlank() -> {
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .clickable(onClick = onSeriesClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seriesTitle,
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
            else -> Spacer(modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        Text(
            text = episode.title,
            color = scheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Episode ${episode.index}",
            color = scheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaMetaChip(text = episode.releaseDate)
            MediaMetaChip(text = episode.rating)
            MediaMetaChip(text = episode.runtime)
            MediaMetaChip(
                text = episode.format,
                background = scheme.primary.copy(alpha = 0.2f),
                border = scheme.primary.copy(alpha = 0.3f),
                textColor = scheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        MediaSynopsis(
            synopsis = episode.synopsis
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row() {
            MediaResumeButton(
                text = if (episode.progress == null) "Play" else "Resume",
                progress = episode.progress?.div(100)?.toFloat() ?: 0f,
                onClick = playAction,
                modifier = Modifier.sizeIn(maxWidth = 200.dp)
            )
            VerticalDivider(
                color = MaterialTheme.colorScheme.secondary,
                thickness = 4.dp,
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row() {
                MediaActionButton(
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    iconColor = MaterialTheme.colorScheme.onSecondary,
                    icon = Icons.Outlined.Add,
                    height = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                MediaActionButton(
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    iconColor = MaterialTheme.colorScheme.onSecondary,
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
            MediaCastRow(
                cast = episode.cast
            )
        }
    }
}
