package hu.bbara.purefin.ui.common.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PurefinIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 52,
    focusedScale: Float = 1f,
    focusedBorderWidth: Dp = 0.dp,
    focusedBorderColor: Color = Color.Transparent,
    focusedBackgroundColor: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    CircularIconButton(
        icon = icon,
        contentDescription = contentDescription,
        containerColor = scheme.surface,
        focusedBackgroundColor = focusedBackgroundColor ?: scheme.surface,
        iconColor = scheme.onSecondary,
        size = size.dp,
        onClick = onClick,
        modifier = modifier,
        focusedScale = focusedScale,
        focusedBorderWidth = focusedBorderWidth,
        focusedBorderColor = focusedBorderColor
    )
}
