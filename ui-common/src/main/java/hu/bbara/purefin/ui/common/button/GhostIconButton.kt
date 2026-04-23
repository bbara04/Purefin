package hu.bbara.purefin.ui.common.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GhostIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusedScale: Float = 1f,
    focusedBorderWidth: Dp = 0.dp,
    focusedBorderColor: Color = Color.Transparent,
    focusedBackgroundColor: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    val backgroundColor = scheme.background.copy(alpha = 0.65f)
    CircularIconButton(
        icon = icon,
        contentDescription = contentDescription,
        containerColor = backgroundColor,
        focusedBackgroundColor = focusedBackgroundColor ?: backgroundColor,
        iconColor = scheme.onBackground,
        size = 52.dp,
        onClick = onClick,
        modifier = modifier,
        focusedScale = focusedScale,
        focusedBorderWidth = focusedBorderWidth,
        focusedBorderColor = focusedBorderColor
    )
}
