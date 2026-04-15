package hu.bbara.purefin.ui.screen.player.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.core.player.model.PlayerUiState

@Composable
internal fun TvPlayerLoadingErrorEndCard(
    uiState: PlayerUiState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlayerLoadingErrorEndCardContent(
        uiState = uiState,
        onRetry = onRetry,
        onNext = onNext,
        onReplay = onReplay,
        onDismissError = onDismissError,
        modifier = modifier,
        showBufferWhileError = false,
        cardPadding = 24.dp,
        cardSpacing = 16.dp,
        backgroundAlpha = 0.92f,
        headlineStyle = MaterialTheme.typography.titleMedium
    )
}
