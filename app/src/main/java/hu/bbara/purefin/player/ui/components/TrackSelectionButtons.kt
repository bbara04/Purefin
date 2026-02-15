package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.common.ui.components.PurefinIconButton
import hu.bbara.purefin.player.model.TrackOption

@Composable
fun QualitySelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        PurefinIconButton(
            icon = Icons.Outlined.HighQuality,
            contentDescription = "Quality",
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 160.dp, max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(scheme.surface.copy(alpha = 0.98f))
                    .widthIn(min = 160.dp, max = 280.dp)
                    .heightIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val selected = option.id == selectedId
                        TrackOptionItem(
                            label = option.label,
                            selected = selected,
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioSelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        PurefinIconButton(
            icon = Icons.Outlined.Language,
            contentDescription = "Audio",
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 160.dp, max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(scheme.surface.copy(alpha = 0.98f))
                    .widthIn(min = 160.dp, max = 280.dp)
                    .heightIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val selected = option.id == selectedId
                        TrackOptionItem(
                            label = option.label,
                            selected = selected,
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubtitlesSelectionButton(
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        PurefinIconButton(
            icon = Icons.Outlined.ClosedCaption,
            contentDescription = "Subtitles",
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 160.dp, max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(scheme.surface.copy(alpha = 0.98f))
                    .widthIn(min = 160.dp, max = 280.dp)
                    .heightIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val selected = option.id == selectedId
                        TrackOptionItem(
                            label = option.label,
                            selected = selected,
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
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
