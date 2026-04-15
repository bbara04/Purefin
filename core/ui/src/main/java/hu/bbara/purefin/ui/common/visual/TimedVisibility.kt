package hu.bbara.purefin.ui.common.visual

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
