package hu.bbara.purefin.common.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class MediaDetailsTopBarShortcut(
    val label: String,
    val onClick: () -> Unit
)

@Composable
internal fun MediaDetailsTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    shortcut: MediaDetailsTopBarShortcut? = null,
    onCastClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    downFocusRequester: FocusRequester? = null
) {
    val downModifier = if (downFocusRequester != null) {
        Modifier.focusProperties { down = downFocusRequester }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                modifier = downModifier
            )
            if (shortcut != null) {
                GhostTextButton(
                    text = shortcut.label,
                    onClick = shortcut.onClick,
                    modifier = downModifier
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(
                icon = Icons.Outlined.Cast,
                contentDescription = "Cast",
                onClick = onCastClick,
                modifier = downModifier
            )
            GhostIconButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "More",
                onClick = onMoreClick,
                modifier = downModifier
            )
        }
    }
}

@Composable
private fun GhostTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    val scheme = MaterialTheme.colorScheme
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else 1.0f, label = "scale")
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) scheme.primary else Color.Transparent,
        label = "border"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(height)
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(percent = 50)
            )
            .clip(RoundedCornerShape(percent = 50))
            .background(if (isFocused) scheme.primary.copy(alpha = 0.25f) else scheme.background.copy(alpha = 0.65f))
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = scheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
