package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.button.PurefinIconButton
import hu.bbara.purefin.player.model.TrackOption

@Composable
fun QualitySelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    overlayController: PersistentOverlayController,
    modifier: Modifier = Modifier
) {
    PurefinIconButton(
        icon = Icons.Outlined.HighQuality,
        contentDescription = "Quality",
        onClick = {
            overlayController.show {
                TrackSelectionPanel(
                    title = "Quality",
                    options = options,
                    selectedId = selectedId,
                    onClose = { overlayController.hide() },
                    onSelect = { option ->
                        onSelect(option)
                        overlayController.hide()
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun AudioSelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    overlayController: PersistentOverlayController,
    modifier: Modifier = Modifier
) {
    PurefinIconButton(
        icon = Icons.Outlined.Language,
        contentDescription = "Audio",
        onClick = {
            overlayController.show {
                TrackSelectionPanel(
                    title = "Audio",
                    options = options,
                    selectedId = selectedId,
                    onClose = { overlayController.hide() },
                    onSelect = { option ->
                        onSelect(option)
                        overlayController.hide()
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun SubtitlesSelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    overlayController: PersistentOverlayController,
    modifier: Modifier = Modifier
) {
    PurefinIconButton(
        icon = Icons.Outlined.ClosedCaption,
        contentDescription = "Subtitles",
        onClick = {
            overlayController.show {
                TrackSelectionPanel(
                    title = "Subtitles",
                    options = options,
                    selectedId = selectedId,
                    onClose = { overlayController.hide() },
                    onSelect = { option ->
                        onSelect(option)
                        overlayController.hide()
                    }
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TrackSelectionPanel(
    title: String,
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .sizeIn(
                    minWidth = 280.dp,
                    maxWidth = 280.dp,
                    minHeight = 220.dp,
                    maxHeight = 360.dp
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Consume taps inside the panel so outside taps dismiss the overlay. */ }
                ),
            color = scheme.surface.copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onClose)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close $title",
                            tint = scheme.onSurface
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 292.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    options.forEach { option ->
                        val selected = option.id == selectedId
                        TrackOptionItem(
                            label = option.label,
                            selected = selected,
                            onClick = { onSelect(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) {
                    scheme.primary.copy(alpha = 0.15f)
                } else {
                    scheme.surfaceVariant.copy(alpha = 0.6f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = scheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
