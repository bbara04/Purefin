package hu.bbara.purefin.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigationManager: ProvidableCompositionLocal<NavigationManager> =
    staticCompositionLocalOf { error("NavigationManager not provided") }

val LocalNavigationBackStack = compositionLocalOf<List<Route>> { emptyList() }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavSharedAnimatedVisibilityScope =
    compositionLocalOf<AnimatedVisibilityScope?> { null }

const val HOME_SEARCH_SHARED_BOUNDS_KEY = "home_search_shared_bounds"
