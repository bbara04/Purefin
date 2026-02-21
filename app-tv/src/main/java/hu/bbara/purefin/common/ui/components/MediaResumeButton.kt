package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    BoxWithConstraints(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(50))
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
    }
}

@Composable
private fun ButtonContent(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.PlayArrow, null, tint = color)
    }
}
