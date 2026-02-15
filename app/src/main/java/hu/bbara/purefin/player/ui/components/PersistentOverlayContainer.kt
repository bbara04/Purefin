package hu.bbara.purefin.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Controller for the persistent overlay that allows components to show content
 * independently of the player controls visibility.
 */
class PersistentOverlayController {
    private var _content by mutableStateOf<(@Composable () -> Unit)?>(null)

    val isVisible: Boolean
        get() = _content != null

    val content: (@Composable () -> Unit)?
        get() = _content

    fun show(content: @Composable () -> Unit) {
        _content = content
    }

    fun hide() {
        _content = null
    }
}

/**
 * A persistent overlay container that sits above the player controls and gesture layer.
 * This allows components to display content (like track selection panels) without being
 * affected by the player controls visibility state.
 *
 * @param controller The controller that manages the overlay's visibility and content
 * @param modifier Modifier to be applied to the container
 */
@Composable
fun PersistentOverlayContainer(
    controller: PersistentOverlayController,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = controller.isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { controller.hide() }
                )
        ) {
            controller.content?.invoke()
        }
    }
}

@Composable
fun rememberPersistentOverlayController(): PersistentOverlayController {
    return remember { PersistentOverlayController() }
}
