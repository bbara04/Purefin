package hu.bbara.purefin.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun TimedVisibility(
    value: Long?,
    hideAfterMillis: Long = 1_000,
    content: @Composable (Long) -> Unit,
) {
    val shownValue = remember { mutableStateOf<Long?>(null) }

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