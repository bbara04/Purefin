package hu.bbara.purefin.tv.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlayerUiState
import hu.bbara.purefin.core.player.model.TrackOption

internal enum class TvTrackPanelType {
    AUDIO,
    SUBTITLES,
    QUALITY
}

@Composable
internal fun TvQualitySelectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.HighQuality,
        contentDescription = "Quality",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
internal fun TvAudioSelectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.Language,
        contentDescription = "Audio",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
internal fun TvSubtitlesSelectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvIconButton(
        icon = Icons.Outlined.ClosedCaption,
        contentDescription = "Subtitles",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
internal fun TvTrackSelectionPanel(
    panelType: TvTrackPanelType,
    uiState: PlayerUiState,
    onSelect: (TrackOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (title, options, selectedId) = when (panelType) {
        TvTrackPanelType.AUDIO -> Triple("Audio", uiState.audioTracks, uiState.selectedAudioTrackId)
        TvTrackPanelType.SUBTITLES -> Triple("Subtitles", uiState.textTracks, uiState.selectedTextTrackId)
        TvTrackPanelType.QUALITY -> Triple("Quality", uiState.qualityTracks, uiState.selectedQualityTrackId)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)),
            color = scheme.surface.copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TvIconButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Close",
                        onClick = onClose
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { option ->
                        TvTrackOptionRow(
                            label = option.label,
                            selected = option.id == selectedId,
                            onClick = { onSelect(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvTrackOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, scheme.primary, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) scheme.primary.copy(alpha = 0.3f)
                else if (selected) scheme.primary.copy(alpha = 0.15f)
                else scheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = scheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
