package hu.bbara.purefin.ui.theme

import androidx.compose.ui.graphics.Color

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color =
    Color.hsl(hue = hue, saturation = saturation / 100f, lightness = lightness / 100f)


val primaryDark = hslColor(260f, 85f, 60f)
val onPrimaryDark = hslColor(255f, 0f, 100f)
val secondaryDark = hslColor(255f, 0f, 40f)
val onSecondaryDark = hslColor(255f, 10f, 88f)
val tertiaryDark = hslColor(255f, 0f, 65f)
val onTertiaryDark = hslColor(255f, 0f, 12f)
val errorDark = hslColor(7f, 100f, 84f)
val onErrorDark = hslColor(357f, 100f, 21f)
val errorContainerDark = hslColor(355f, 100f, 29f)
val onErrorContainerDark = hslColor(6f, 100f, 92f)
val backgroundDark = hslColor(255f, 10f, 8f)
val onBackgroundDark = hslColor(255f, 60f, 94f)
val surfaceDark = hslColor(255f, 10f, 16f)
val onSurfaceDark = hslColor(255f, 10f, 92f)
val primaryContainerDark = primaryDark
val onPrimaryContainerDark = onPrimaryDark
val secondaryContainerDark = secondaryDark
val onSecondaryContainerDark = onSecondaryDark
val tertiaryContainerDark = tertiaryDark
val onTertiaryContainerDark = onTertiaryDark
val surfaceContainerDark = surfaceDark
