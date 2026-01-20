package hu.bbara.purefin.app.home.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class HomeColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val drawerBackground: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val avatarBackground: Color,
    val avatarBorder: Color,
    val drawerFooterBackground: Color
)

@Composable
fun rememberHomeColors(): HomeColors {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        HomeColors(
            primary = scheme.primary,
            onPrimary = scheme.onPrimary,
            background = scheme.background,
            drawerBackground = scheme.surface,
            card = scheme.surfaceVariant,
            textPrimary = scheme.onBackground,
            textSecondary = scheme.onSurfaceVariant,
            divider = scheme.outlineVariant,
            avatarBackground = scheme.primaryContainer,
            avatarBorder = scheme.outline,
            drawerFooterBackground = scheme.surfaceVariant
        )
    }
}
