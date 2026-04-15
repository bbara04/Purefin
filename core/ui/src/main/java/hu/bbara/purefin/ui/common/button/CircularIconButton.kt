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

@Composable
internal fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    iconColor: Color,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusedScale: Float = 1f,
    focusedBorderWidth: Dp,
    focusedBorderColor: Color,
    focusedBackgroundColor: Color = containerColor
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        label = "scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) focusedBorderColor else Color.Transparent,
        label = "border"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) focusedBackgroundColor else containerColor,
        label = "background"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(size)
            .border(
                width = if (isFocused) focusedBorderWidth else focusedBorderWidth * 0,
                color = borderColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}
