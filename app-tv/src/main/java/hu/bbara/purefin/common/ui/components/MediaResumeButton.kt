package hu.bbara.purefin.common.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaResumeButton(
    text: String,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val primaryColor = scheme.primary
    val onPrimaryColor = scheme.onPrimary
    val focusShape = RoundedCornerShape(50)
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.08f else 1.0f, label = "scale")
    val focusBorderColor by animateColorAsState(
        targetValue = if (isFocused) scheme.onBackground else Color.Transparent,
        label = "focus-border"
    )
    val focusHaloColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary.copy(alpha = 0.22f) else Color.Transparent,
        label = "focus-halo"
    )

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(52.dp)
            .background(focusHaloColor, focusShape)
            .border(3.dp, focusBorderColor, focusShape)
            .clip(focusShape)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
    ) {
        // Bottom layer: inverted colors (visible for the remaining %)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(onPrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            ButtonContent(text = text, color = primaryColor)
        }

        // Top layer: primary colors, clipped to the progress %
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val clipWidth = size.width * progress
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = clipWidth,
                        bottom = size.height
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            ButtonContent(text = text, color = onPrimaryColor)
        }

        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, scheme.primary.copy(alpha = 0.95f), focusShape)
            )
        }
    }
}

@Composable
private fun ButtonContent(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.PlayArrow, null, tint = color)
    }
}
