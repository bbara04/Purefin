package hu.bbara.purefin.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.player.model.PlayerUiState

@Composable
fun PlayerLoadingErrorEndCard(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onDismissError: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier) {
        AnimatedVisibility(visible = uiState.isBuffering) {
            CircularProgressIndicator(color = scheme.primary)
        }

        AnimatedVisibility(visible = uiState.error != null) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.background.copy(alpha = 0.9f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = uiState.error ?: "Playback error",
                    color = scheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                    Button(
                        onClick = onDismissError,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.surface)
                    ) {
                        Text("Dismiss", color = scheme.onSurface)
                    }
                }
            }
        }

        AnimatedVisibility(visible = uiState.isEnded && uiState.error == null && !uiState.isBuffering) {
            val nextUp = uiState.queue.getOrNull(
                uiState.queue.indexOfFirst { it.isCurrent }.takeIf { it >= 0 }?.plus(1) ?: -1
            )
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.background.copy(alpha = 0.9f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (nextUp != null) {
                    Text(
                        text = "Up next",
                        color = scheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = nextUp.title,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = onNext) {
                        Text("Play next")
                    }
                } else {
                    Text(text = "Playback finished", color = scheme.onBackground)
                    Button(onClick = onReplay) {
                        Text("Replay")
                    }
                }
            }
        }
    }
}
