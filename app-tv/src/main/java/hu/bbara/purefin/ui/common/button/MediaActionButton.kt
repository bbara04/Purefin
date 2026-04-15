package hu.bbara.purefin.ui.common.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MediaActionButton(
    backgroundColor: Color,
    iconColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    height: Dp,
    onClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.1f else 1.0f, label = "scale")
    val borderColor by animateColorAsState(targetValue = if (isFocused) scheme.primary else Color.Transparent, label = "border")

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(height)
            .border(if (isFocused) 2.5.dp else 0.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.6f))
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor)
    }
}
