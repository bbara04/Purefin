package hu.bbara.purefin.common.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Async image that falls back to theme-synced color blocks so loading/error states
 * stay aligned with PurefinTheme's colorScheme.
 */
@Composable
fun PurefinAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val placeholderPainter = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)

    // Convert empty string to null to properly trigger fallback
    val effectiveModel = when {
        model is String && model.isEmpty() -> null
        else -> model
    }

    AsyncImage(
        model = effectiveModel,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholderPainter,
        error = placeholderPainter,
        fallback = placeholderPainter
    )
}
