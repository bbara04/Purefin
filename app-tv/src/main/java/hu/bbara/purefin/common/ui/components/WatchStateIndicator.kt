package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WatchStateIndicator(
    watched: Boolean,
    started: Boolean,
    watchedColor: Color = MaterialTheme.colorScheme.onPrimary,
    watchedBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    startedColor: Color = MaterialTheme.colorScheme.onSecondary,
    startedBackgroundColor: Color = MaterialTheme.colorScheme.secondary,
    size: Int = 24,
    modifier: Modifier = Modifier
) {

    if (watched.not() && started.not()) {
        return
    }

    val foregroundColor = if (watched) watchedColor.copy(alpha = 0.8f) else startedColor.copy(alpha = 0.3f)
    val backgroundColor = if (watched) watchedBackgroundColor.copy(alpha = 0.8f) else startedBackgroundColor.copy(alpha = 0.3f)
    val borderColor = if (watched) watchedBackgroundColor.copy(alpha = 0.8f) else startedBackgroundColor.copy(alpha = 0.8f)


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(1.dp, borderColor, CircleShape)
            .background(backgroundColor, CircleShape)
            .size(size.dp)
            .clip(CircleShape)
    ) {
        if (watched) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Check",
                tint = foregroundColor,
                modifier = Modifier
                    .padding(1.dp)
                    .matchParentSize()
            )
        }
    }
}

@Preview
@Composable
private fun WatchStateIndicatorPreview() {
    Column() {
        WatchStateIndicator(
            watched = false,
            started = false
        )
        WatchStateIndicator(
            watched = true,
            started = false
        )
        WatchStateIndicator(
            watched = false,
            started = true
        )
    }
}
