package hu.bbara.purefin.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.PlayerUiState
import hu.bbara.purefin.player.model.TrackOption

@Composable
fun PlayerSettingsSheet(
    visible: Boolean,
    uiState: PlayerUiState,
    onDismiss: () -> Unit,
    onSelectTrack: (TrackOption) -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = scheme.surface.copy(alpha = 0.98f)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Playback settings", color = scheme.onSurface, style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = scheme.onSurface,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onDismiss() }
                                .padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TrackGroup(
                        label = "Audio track",
                        icon = { Icon(Icons.Outlined.Language, contentDescription = null, tint = scheme.onSurface) },
                        options = uiState.audioTracks,
                        selectedId = uiState.selectedAudioTrackId,
                        onSelect = onSelectTrack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrackGroup(
                        label = "Subtitles",
                        icon = { Icon(Icons.Outlined.ClosedCaption, contentDescription = null, tint = scheme.onSurface) },
                        options = uiState.textTracks,
                        selectedId = uiState.selectedTextTrackId,
                        onSelect = onSelectTrack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrackGroup(
                        label = "Quality",
                        icon = { Icon(Icons.Outlined.HighQuality, contentDescription = null, tint = scheme.onSurface) },
                        options = uiState.qualityTracks,
                        selectedId = uiState.selectedQualityTrackId,
                        onSelect = onSelectTrack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SpeedGroup(
                        selectedSpeed = uiState.playbackSpeed,
                        onSpeedSelected = onSpeedSelected
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TrackGroup(
    label: String,
    icon: @Composable () -> Unit,
    options: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(text = label, color = scheme.onSurface, style = MaterialTheme.typography.titleSmall)
        }
        FlowChips(
            items = options,
            selectedId = selectedId,
            onSelect = onSelect
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChips(
    items: List<TrackOption>,
    selectedId: String?,
    onSelect: (TrackOption) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { option ->
            val selected = option.id == selectedId
            Text(
                text = option.label,
                color = if (selected) scheme.onPrimary else scheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) scheme.primary else scheme.surfaceVariant)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SpeedGroup(
    selectedSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val options = listOf(0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Speed, contentDescription = null, tint = scheme.onSurface)
            Text(text = "Playback speed", color = scheme.onSurface, style = MaterialTheme.typography.titleSmall)
        }
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { speed ->
                val selected = speed == selectedSpeed
                Text(
                    text = "${speed}x",
                    color = if (selected) scheme.onPrimary else scheme.onSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) scheme.primary else scheme.surfaceVariant)
                        .clickable { onSpeedSelected(speed) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
