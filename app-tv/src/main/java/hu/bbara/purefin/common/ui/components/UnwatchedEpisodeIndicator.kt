package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnwatchedEpisodeIndicator(
    unwatchedCount: Int,
    foregroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    size: Int = 24,
    modifier: Modifier = Modifier
) {
    if (unwatchedCount == 0) {
        return
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(1.dp, backgroundColor.copy(alpha = 0.8f), CircleShape)
            .background(backgroundColor.copy(alpha = 0.8f), CircleShape)
            .size(size.dp)
            .clip(CircleShape)
    ) {
        Text(
            text = if (unwatchedCount > 9) "9+" else unwatchedCount.toString(),
            color = foregroundColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.W900,
            fontSize = 15.sp
        )
    }
}
