package hu.bbara.purefin.ui.screen.download.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.core.feature.downloads.DownloadsViewModel
import hu.bbara.purefin.ui.common.card.PosterCard

@Composable
fun DownloadsContent(
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val downloads = viewModel.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeDownloads = viewModel.activeDownloads.collectAsStateWithLifecycle()

    val isEmpty = downloads.value.isEmpty() && activeDownloads.value.isEmpty()

    if (isEmpty) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag(DownloadsEmptyStateTag),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No downloads yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .testTag(DownloadsGridTag)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeDownloads.value.isNotEmpty()) {
            item(key = "downloading-header", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Downloading",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            items(
                items = activeDownloads.value,
                key = { it.contentId },
                span = { GridItemSpan(maxLineSpan) }
            ) { item ->
                DownloadingItemRow(
                    item = item,
                    onCancel = { viewModel.cancelDownload(it) },
                    modifier = Modifier.testTag("$DownloadsActiveItemTagPrefix${item.contentId}")
                )
            }
            if (downloads.value.isNotEmpty()) {
                item(key = "downloaded-header", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Downloaded",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        items(downloads.value, key = { item -> item.media.id }) { item ->
            Column(
                modifier = Modifier.testTag("$DownloadsItemTagPrefix${item.media.id}")
            ) {
                PosterCard(
                    item = item.media,
                    onMovieSelected = viewModel::onMovieSelected,
                    onSeriesSelected = viewModel::onSeriesSelected,
                    onEpisodeSelected = { _, _, _ -> },
                )
            }
        }
    }
}

internal const val DownloadsEmptyStateTag = "downloads-empty-state"
internal const val DownloadsGridTag = "downloads-grid"
internal const val DownloadsActiveItemTagPrefix = "downloads-active-item-"
internal const val DownloadsItemTagPrefix = "downloads-item-"
