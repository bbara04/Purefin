package hu.bbara.purefin.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PlayerAdjustmentIndicator(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    value: Float,
    sliderHeight: Dp = 140.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val percent = (value.coerceIn(0f, 1f) * 100).roundToInt()
    val clamped = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(sliderHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(scheme.onSurface.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(10.dp)
                        .height(sliderHeight * clamped)
                        .clip(RoundedCornerShape(5.dp))
                        .background(scheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$percent%",
                color = scheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
