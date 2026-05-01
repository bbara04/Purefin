package hu.bbara.purefin.ui.common.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = backgroundColor,
            contentColor = iconColor
        ),
        shape = CircleShape,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(size)
            .border(
                width = if (isFocused) focusedBorderWidth else focusedBorderWidth * 0,
                color = borderColor,
                shape = CircleShape
            )
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
