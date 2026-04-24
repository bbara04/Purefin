package hu.bbara.purefin.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigationManager: ProvidableCompositionLocal<NavigationManager> =
    staticCompositionLocalOf { error("NavigationManager not provided") }

val LocalNavigationBackStack = compositionLocalOf<List<Route>> { emptyList() }
