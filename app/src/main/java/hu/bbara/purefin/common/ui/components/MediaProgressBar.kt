package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A progress bar component for displaying media playback progress.
 *
 * @param progress The progress value between 0f and 1f, where 0f is no progress and 1f is complete.
 * @param foregroundColor The color of the progress indicator.
 * @param backgroundColor The color of the background/unfilled portion of the progress bar.
 * @param modifier The modifier to be applied to the Box. Modifier should contain the Alignment.
 */
@Composable
fun MediaProgressBar(
    progress: Float,
    foregroundColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .height(4.dp)
            .background(backgroundColor.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(foregroundColor)
        )
    }
}