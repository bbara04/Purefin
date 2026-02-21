package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaPlaybackSettings(
    backgroundColor: Color,
    foregroundColor: Color,
    audioTrack: String,
    subtitles: String,
    audioIcon: ImageVector = Icons.Outlined.VolumeUp,
    subtitleIcon: ImageVector = Icons.Outlined.ClosedCaption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        MediaSettingDropdown(
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor,
            label = "Audio Track",
            value = audioTrack,
            icon = audioIcon
        )
        Spacer(modifier = Modifier.height(12.dp))
        MediaSettingDropdown(
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor,
            label = "Subtitles",
            value = subtitles,
            icon = subtitleIcon
        )
    }
}

@Composable
private fun MediaSettingDropdown(
    backgroundColor: Color,
    foregroundColor: Color,
    label: String,
    value: String,
    icon: ImageVector
) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = foregroundColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = value, color = foregroundColor, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = foregroundColor
            )
        }
    }
}