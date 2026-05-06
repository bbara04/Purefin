package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.core.player.model.PlayerUiState

@Composable
fun PlayerLoadingErrorEndCard(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onDismissError: () -> Unit
) {
    PlayerLoadingErrorEndCardContent(
        uiState = uiState,
        onRetry = onRetry,
        onNext = onNext,
        onReplay = onReplay,
        onDismissError = onDismissError,
        modifier = modifier,
        showBufferWhileError = true,
        headlineStyle = MaterialTheme.typography.bodyLarge
    )
}
