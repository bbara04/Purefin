package hu.bbara.purefin.ui.common.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PurefinAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector? = Icons.Outlined.BrokenImage
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val placeholderPainter = ColorPainter(surfaceVariant)
    val effectiveModel = when {
        model is String && model.isEmpty() -> null
        else -> model
    }
    var showFallbackIcon by remember(effectiveModel, fallbackIcon) {
        mutableStateOf(effectiveModel == null && fallbackIcon != null)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = effectiveModel,
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = placeholderPainter,
            error = placeholderPainter,
            fallback = placeholderPainter,
            onLoading = { showFallbackIcon = false },
            onSuccess = { showFallbackIcon = false },
            onError = { showFallbackIcon = fallbackIcon != null },
        )
        if (showFallbackIcon && fallbackIcon != null) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
