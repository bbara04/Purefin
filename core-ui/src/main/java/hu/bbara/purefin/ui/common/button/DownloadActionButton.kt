package hu.bbara.purefin.ui.common.button

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.download.DownloadState

@Composable
fun DownloadActionButton(
    downloadState: DownloadState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
) {
    MediaActionButton(
        backgroundColor = MaterialTheme.colorScheme.surface,
        iconColor = MaterialTheme.colorScheme.onSurface,
        icon = when (downloadState) {
            is DownloadState.NotDownloaded -> Icons.Outlined.Download
            is DownloadState.Downloading -> Icons.Outlined.Close
            is DownloadState.Downloaded -> Icons.Outlined.DownloadDone
            is DownloadState.Failed -> Icons.Outlined.Download
        },
        height = height,
        modifier = modifier,
        onClick = onClick
    )
}
