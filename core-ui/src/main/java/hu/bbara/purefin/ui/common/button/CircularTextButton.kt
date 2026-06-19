package hu.bbara.purefin.ui.common.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularTextButton(
    text: String,
    textColor: Color,
    fontSize: Int,
    containerColor: Color,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusedScale: Float = 1f,
    focusedBackgroundColor: Color = containerColor
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        label = "scale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) focusedBackgroundColor else containerColor,
        label = "background"
    )

    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = CircleShape,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(size)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
