package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun TvIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Int = 52,
    enabled: Boolean = true,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        label = "scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary else Color.Transparent,
        label = "border"
    )
    val shape = RoundedCornerShape(50)
    val iconSize = (size - 24).coerceAtLeast(0).dp

    val buttonModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .alpha(if (enabled) 1f else 0.4f)
        .border(
            width = if (isFocused) 2.dp else 0.dp,
            color = borderColor,
            shape = shape
        )
        .clip(shape)
        .background(
            if (isFocused) scheme.primary.copy(alpha = 0.5f)
            else scheme.background.copy(alpha = 0.65f)
        )
        .focusProperties { canFocus = enabled }
        .semantics {
            if (!enabled) {
                disabled()
            }
        }
        .onFocusChanged { isFocused = it.isFocused }
        .clickable(enabled = enabled) { onClick() }

    if (label == null) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = scheme.onBackground,
            modifier = buttonModifier
                .size(size.dp)
                .padding(12.dp)
                .size(iconSize)
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = buttonModifier
                .widthIn(min = 104.dp)
                .height(size.dp)
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = scheme.onBackground,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = label,
                color = scheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
