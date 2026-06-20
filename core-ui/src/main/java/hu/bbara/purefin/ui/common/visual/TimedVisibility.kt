package hu.bbara.purefin.ui.common.visual

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> EmptyValueTimedVisibility(
    value: T?,
    hideAfterMillis: Long = 1_000,
    enterTransition: EnterTransition = EnterTransition.None,
    exitTransition: ExitTransition = ExitTransition.None,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    var displayedValue by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        if (value != null) {
            displayedValue = value
        }
    }
    if (displayedValue != null) {
        ValueChangeTimedVisibility(
            value = displayedValue!!,
            hideAfterMillis = hideAfterMillis,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            modifier = modifier,
            content = content
        )
    }
}

@Composable
fun <T> ValueChangeTimedVisibility(
    value: T,
    hideAfterMillis: Long = 1_000,
    enterTransition: EnterTransition = EnterTransition.None,
    exitTransition: ExitTransition = ExitTransition.None,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    var displayedValue by remember { mutableStateOf(value) }
    var hasInitialValue by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var hideJob: Job? by remember { mutableStateOf(null) }
    fun restartHideJob() {
        hideJob?.cancel()
        hideJob = coroutineScope.launch {
            delay(hideAfterMillis)
            isVisible = false
        }
    }

    LaunchedEffect(value) {
        if (!hasInitialValue) {
            hasInitialValue = true
            return@LaunchedEffect
        }
        displayedValue = value
        isVisible = true
        restartHideJob()
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = enterTransition,
        exit = exitTransition
    ) {
        content(displayedValue)
    }
}
