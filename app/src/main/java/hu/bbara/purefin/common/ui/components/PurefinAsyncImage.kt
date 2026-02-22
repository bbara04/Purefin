package hu.bbara.purefin.common.ui.components

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

/**
 * Async image that falls back to theme-synced color blocks so loading/error states
 * stay aligned with PurefinTheme's colorScheme.
 *
 * Uses plain [AsyncImage] (no SubcomposeLayout) so it is safe inside lazy lists and
 * composables that query intrinsic measurements (e.g. [ListItem]).
 *
 * - Loading: solid surfaceVariant block
 * - Error / missing URL: surfaceVariant block with a centered [fallbackIcon]
 */
@Composable
fun PurefinAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackIcon: ImageVector = Icons.Outlined.BrokenImage
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Convert empty string to null to properly trigger fallback
    val effectiveModel = if (model is String && model.isEmpty()) null else model

    // Show icon immediately for null model (no request will be made); callbacks update it otherwise.
    var showFallbackIcon by remember(effectiveModel) { mutableStateOf(effectiveModel == null) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = effectiveModel,
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = ColorPainter(surfaceVariant),
            error = ColorPainter(surfaceVariant),
            fallback = ColorPainter(surfaceVariant),
            onLoading = { showFallbackIcon = false },
            onSuccess = { showFallbackIcon = false },
            onError = { showFallbackIcon = true },
        )
        if (showFallbackIcon) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
