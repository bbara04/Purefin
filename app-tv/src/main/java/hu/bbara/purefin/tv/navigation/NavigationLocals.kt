package hu.bbara.purefin.tv.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import hu.bbara.purefin.feature.shared.navigation.NavigationManager
import hu.bbara.purefin.feature.shared.navigation.Route

val LocalNavigationManager: ProvidableCompositionLocal<NavigationManager> =
    staticCompositionLocalOf { error("NavigationManager not provided") }

val LocalNavigationBackStack = compositionLocalOf<List<Route>> { emptyList() }
