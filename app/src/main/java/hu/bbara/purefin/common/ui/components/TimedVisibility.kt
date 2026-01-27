package hu.bbara.purefin.common.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * A composable that displays content for a specified duration after the value becomes null.
 *
 * @param value The value to display. When set to a non-null value, it will be shown immediately.
 *              When set to null, the previously shown value will remain visible for [hideAfterMillis]
 *              before being hidden.
 * @param hideAfterMillis The duration in milliseconds to keep showing the last value after [value] becomes null.
 *                        Defaults to 1000ms (1 second).
 * @param content The composable content to display, receiving the current non-null value.
 */
@Composable
fun <T> EmptyValueTimedVisibility(
    value: T?,
    hideAfterMillis: Long = 1_000,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val shownValue = remember { mutableStateOf<T?>(null) }

    LaunchedEffect(value) {
        if (value == null) {
            delay(hideAfterMillis)
            shownValue.value = null
        }
        shownValue.value = value
    }

    shownValue.value?.let {
        content(it)
    }
}

/**
 * Displays [content] whenever [value] changes and hides it after [hideAfterMillis]
 * milliseconds without further updates.
 *
 * @param value The value whose changes should trigger visibility.
 * @param hideAfterMillis Duration in milliseconds after which the content will be hidden
 *                        if [value] has not changed again.
 * @param content The composable to render while visible.
 */
@Composable
fun <T> ValueChangeTimedVisibility(
    value: T,
    hideAfterMillis: Long = 1_000,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    var displayedValue by remember { mutableStateOf(value) }
    var isVisible by remember { mutableStateOf(false) }
    var hasInitialValue by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        displayedValue = value
        if (!hasInitialValue) {
            hasInitialValue = true
            return@LaunchedEffect
        }

        isVisible = true
        delay(hideAfterMillis)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        content(displayedValue)
    }
}
