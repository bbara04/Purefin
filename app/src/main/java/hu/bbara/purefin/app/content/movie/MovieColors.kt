package hu.bbara.purefin.app.content.movie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

internal data class MovieColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textMutedStrong: Color
)

@Composable
internal fun rememberMovieColors(): MovieColors {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        MovieColors(
            primary = scheme.primary,
            onPrimary = scheme.onPrimary,
            background = scheme.background,
            surface = scheme.surface,
            surfaceAlt = scheme.surfaceVariant,
            surfaceBorder = scheme.outlineVariant,
            textPrimary = scheme.onBackground,
            textSecondary = scheme.onSurface,
            textMuted = scheme.onSurfaceVariant,
            textMutedStrong = scheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
