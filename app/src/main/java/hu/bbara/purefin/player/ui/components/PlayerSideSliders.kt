package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PlayerSideSliders(
    modifier: Modifier = Modifier,
    brightness: Float,
    volume: Float,
    showBrightness: Boolean,
    showVolume: Boolean,
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier.fillMaxWidth()) {
        if (showBrightness) {
            SideOverlay(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                icon = { Icon(Icons.Outlined.BrightnessMedium, contentDescription = null, tint = scheme.onBackground) },
                progress = brightness,
                scheme = scheme
            )
        }
        if (showVolume) {
            SideOverlay(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                icon = { Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = scheme.onBackground) },
                progress = volume,
                scheme = scheme
            )
        }
    }
}

@Composable
private fun SideOverlay(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    progress: Float,
    scheme: androidx.compose.material3.ColorScheme
) {
    Column(
        modifier = modifier
            .fillMaxHeight(0.3f)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.background.copy(alpha = 0.8f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            color = scheme.primary,
            trackColor = scheme.onBackground.copy(alpha = 0.2f)
        )
    }
}
