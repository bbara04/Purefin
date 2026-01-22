package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

@Composable
fun MediaActionButton(
    backgroundColor: Color,
    iconColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    height: Dp,
) {
    Box(
        modifier = modifier
            .size(height)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.6f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor)
    }
}