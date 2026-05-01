package hu.bbara.purefin.ui.common.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    focusedScale: Float = 1f,
    focusedBorderWidth: Dp = 0.dp,
    focusedBorderColor: Color = Color.Transparent,
    focusedBackgroundColor: Color? = null
) {
    CircularIconButton(
        icon = icon,
        contentDescription = null,
        containerColor = backgroundColor,
        focusedBackgroundColor = focusedBackgroundColor ?: backgroundColor,
        iconColor = iconColor,
        size = height,
        onClick = onClick,
        modifier = modifier,
        focusedScale = focusedScale,
        focusedBorderWidth = focusedBorderWidth,
        focusedBorderColor = focusedBorderColor
    )
}
