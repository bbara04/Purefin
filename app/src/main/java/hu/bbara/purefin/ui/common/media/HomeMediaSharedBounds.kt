package hu.bbara.purefin.ui.common.media

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import hu.bbara.purefin.navigation.LocalHomeMediaSharedBoundsKey
import hu.bbara.purefin.navigation.LocalNavSharedAnimatedVisibilityScope
import hu.bbara.purefin.navigation.LocalSetHomeMediaSharedBoundsKey
import hu.bbara.purefin.navigation.LocalSharedTransitionScope
import java.util.UUID

fun homeMediaSharedBoundsKey(origin: String, mediaId: UUID): String =
    "home_media_${origin}_$mediaId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.homeMediaSharedBoundsSource(sharedBoundsKey: String): Modifier {
    val selectedKey = LocalHomeMediaSharedBoundsKey.current
    return homeMediaSharedBounds(sharedBoundsKey.takeIf { it == selectedKey })
}

@Composable
fun Modifier.homeMediaSharedBoundsDestination(): Modifier =
    homeMediaSharedBounds(LocalHomeMediaSharedBoundsKey.current)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun isHomeMediaSharedBoundsTransitionActive(): Boolean {
    val sharedBoundsKey = LocalHomeMediaSharedBoundsKey.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    return sharedBoundsKey != null && sharedTransitionScope?.isTransitionActive == true
}

@Composable
fun rememberHomeMediaSharedBoundsClick(
    sharedBoundsKey: String,
    onClick: () -> Unit
): () -> Unit {
    val selectSharedBoundsKey = LocalSetHomeMediaSharedBoundsKey.current
    return remember(sharedBoundsKey, onClick, selectSharedBoundsKey) {
        {
            selectSharedBoundsKey(sharedBoundsKey)
            onClick()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.homeMediaSharedBounds(sharedBoundsKey: String?): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavSharedAnimatedVisibilityScope.current

    if (
        sharedBoundsKey == null ||
        sharedTransitionScope == null ||
        animatedVisibilityScope == null
    ) {
        return this
    }

    return with(sharedTransitionScope) {
        this@homeMediaSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = sharedBoundsKey),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = fadeIn(),
            exit = fadeOut(),
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
        )
    }
}
