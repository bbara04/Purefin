package hu.bbara.purefin.app.content.movie

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bbara.purefin.common.ui.MediaCastRow
import hu.bbara.purefin.common.ui.MediaMetaChip
import hu.bbara.purefin.common.ui.MediaSynopsis
import hu.bbara.purefin.common.ui.components.GhostIconButton
import hu.bbara.purefin.common.ui.components.MediaActionButton
import hu.bbara.purefin.common.ui.components.MediaPlaybackSettings
import hu.bbara.purefin.common.ui.components.MediaResumeButton
import hu.bbara.purefin.download.DownloadState
import hu.bbara.purefin.player.PlayerActivity

@Composable
internal fun MovieTopBar(
    onBack: () -> Unit,
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(icon = Icons.Outlined.Cast, contentDescription = "Cast", onClick = { })
            GhostIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = { })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MovieDetails(
    movie: MovieUiModel,
    downloadState: DownloadState,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val playAction = remember(movie.id) {
        {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("MEDIA_ID", movie.id.toString())
            context.startActivity(intent)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = movie.title,
            color = scheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaMetaChip(text = movie.year)
            MediaMetaChip(text = movie.rating)
            MediaMetaChip(text = movie.runtime)
            MediaMetaChip(
                text = movie.format,
                background = scheme.primary.copy(alpha = 0.2f),
                border = scheme.primary.copy(alpha = 0.3f),
                textColor = scheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        MediaSynopsis(
            synopsis = movie.synopsis
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row() {
            MediaResumeButton(
                text = if (movie.progress == null) "Play" else "Resume",
                progress = movie.progress?.div(100)?.toFloat() ?: 0f,
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
            audioTrack = movie.audioTrack,
            subtitles = movie.subtitles
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Cast",
            color = scheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        MediaCastRow(
            //TODO fix it
            cast = emptyList()
        )
    }
}
