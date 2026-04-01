package hu.bbara.purefin.ui.theme

import androidx.compose.ui.graphics.Color

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color =
    Color.hsl(hue = hue, saturation = saturation / 100f, lightness = lightness / 100f)

val primaryDark = hslColor(255f, 70f, 70f)
val onPrimaryDark = hslColor(255f, 60f, 12f)
val primaryContainerDark = hslColor(255f, 70f, 70f)
val onPrimaryContainerDark = hslColor(255f, 60f, 12f)
val secondaryDark = hslColor(255f, 40f, 40f)
val onSecondaryDark = hslColor(255f, 40f, 88f)
val secondaryContainerDark = hslColor(255f, 40f, 40f)
val onSecondaryContainerDark = hslColor(255f, 40f, 88f)
val tertiaryDark = hslColor(255f, 15f, 65f)
val onTertiaryDark = hslColor(255f, 15f, 12f)
val tertiaryContainerDark = hslColor(255f, 15f, 65.0f)
val onTertiaryContainerDark = hslColor(255f, 15f, 12f)
val errorDark = hslColor(7f, 100f, 84f)
val onErrorDark = hslColor(357f, 100f, 21f)
val errorContainerDark = hslColor(355f, 100f, 29f)
val onErrorContainerDark = hslColor(6f, 100f, 92f)
val backgroundDark = hslColor(255f, 60f, 8f)
val onBackgroundDark = hslColor(255f, 60f, 94f)
val surfaceDark = hslColor(255f, 40f, 12f)
val onSurfaceDark = hslColor(255f, 40f, 92f)
val surfaceContainerDark = hslColor(255f, 40f, 12f)