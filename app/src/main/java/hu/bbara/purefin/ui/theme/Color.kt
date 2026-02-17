package hu.bbara.purefin.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

/**
 * HSL Color Palette
 * Primary: Purple (270°)
 * Secondary: Teal (180°)
 * Tertiary: Orange (30°)
 */

// Light Mode Palette
val LightPrimary = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.65f, 0.45f)))
val LightOnPrimary = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, 1f)))
val LightPrimaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.60f, 0.90f)))
val LightOnPrimaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.70f, 0.15f)))

val LightSecondary = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.55f, 0.40f)))
val LightOnSecondary = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, 1f)))
val LightSecondaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.50f, 0.88f)))
val LightOnSecondaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.65f, 0.15f)))

val LightTertiary = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.75f, 0.50f)))
val LightOnTertiary = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, 1f)))
val LightTertiaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.70f, 0.92f)))
val LightOnTertiaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.80f, 0.18f)))

val LightBackground = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.15f, 0.98f)))
val LightOnBackground = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.20f, 0.12f)))
val LightSurface = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.15f, 0.98f)))
val LightOnSurface = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.20f, 0.12f)))

// Dark Mode Palette
val DarkPrimary = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.60f, 0.75f)))
val DarkOnPrimary = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.70f, 0.20f)))
val DarkPrimaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.55f, 0.30f)))
val DarkOnPrimaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.50f, 0.92f)))

val DarkSecondary = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.50f, 0.70f)))
val DarkOnSecondary = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.65f, 0.18f)))
val DarkSecondaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.50f, 0.28f)))
val DarkOnSecondaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(180f, 0.45f, 0.90f)))

val DarkTertiary = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.70f, 0.72f)))
val DarkOnTertiary = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.80f, 0.22f)))
val DarkTertiaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.65f, 0.32f)))
val DarkOnTertiaryContainer = Color(ColorUtils.HSLToColor(floatArrayOf(30f, 0.65f, 0.93f)))

val DarkBackground = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.18f, 0.08f)))
val DarkOnBackground = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.10f, 0.92f)))
val DarkSurface = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.18f, 0.14f)))
val DarkOnSurface = Color(ColorUtils.HSLToColor(floatArrayOf(270f, 0.10f, 0.86f)))