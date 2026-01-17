package hu.bbara.purefin.app.home

import androidx.compose.foundation.isSystemInDarkTheme
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
fun rememberHomeColors(isDark: Boolean = isSystemInDarkTheme()): HomeColors {
    val primary = Color(0xFFDDA73C)
    return remember(isDark) {
        HomeColors(
            primary = primary,
            onPrimary = Color(0xFF17171C),
            background = if (isDark) Color(0xFF17171C) else Color(0xFFF8F7F6),
            drawerBackground = if (isDark) Color(0xFF1E1E24) else Color(0xFFF8F7F6),
            card = Color(0xFF24242B),
            textPrimary = if (isDark) Color.White else Color(0xFF141517),
            textSecondary = if (isDark) Color(0xFF9AA0A6) else Color(0xFF6B7280),
            divider = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
            avatarBackground = Color(0xFF3A3A46),
            avatarBorder = primary.copy(alpha = 0.3f),
            drawerFooterBackground = if (isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f)
        )
    }
}
