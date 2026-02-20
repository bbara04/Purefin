package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.PurefinIconButton
import hu.bbara.purefin.core.player.model.TrackOption

@Composable
fun QualitySelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    overlayController: PersistentOverlayController,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    PurefinIconButton(
        icon = Icons.Outlined.HighQuality,
        contentDescription = "Quality",
        onClick = {
            overlayController.show {
                TrackSelectionPanel(
                    title = "Quality",
                    options = options,
                    selectedId = selectedId,
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
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Prevent clicks from bubbling */ }
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scheme.surface.copy(alpha = 0.98f))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = scheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            .clip(RoundedCornerShape(10.dp))
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
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
