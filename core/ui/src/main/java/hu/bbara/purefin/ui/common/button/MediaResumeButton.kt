package hu.bbara.purefin.ui.common.button

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaResumeButton(
    text: String,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusedScale: Float = 1f,
    focusHaloColor: Color = Color.Transparent,
    focusBorderWidth: Dp = 0.dp,
    focusBorderColor: Color = Color.Transparent,
    overlayBorderWidth: Dp = 0.dp,
    overlayBorderColor: Color = Color.Transparent,
    focusable: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val primaryColor = scheme.primary
    val onPrimaryColor = scheme.onPrimary
    val shape = RoundedCornerShape(50)
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        label = "scale"
    )
    val haloColor by animateColorAsState(
        targetValue = if (isFocused) focusHaloColor else Color.Transparent,
        label = "focus-halo"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) focusBorderColor else Color.Transparent,
        label = "focus-border"
    )

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(52.dp)
            .background(haloColor, shape)
            .border(
                width = if (isFocused) focusBorderWidth else 0.dp,
                color = borderColor,
                shape = shape
            )
            .clip(shape)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .clickable(onClick = onClick)
            .then(if (focusable) Modifier.focusable() else Modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(onPrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            ResumeButtonContent(text = text, color = primaryColor)
        }

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
            ResumeButtonContent(text = text, color = onPrimaryColor)
        }

        if (isFocused && overlayBorderWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(overlayBorderWidth, overlayBorderColor, shape)
            )
        }
    }
}

@Composable
private fun ResumeButtonContent(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.PlayArrow, null, tint = color)
    }
}
