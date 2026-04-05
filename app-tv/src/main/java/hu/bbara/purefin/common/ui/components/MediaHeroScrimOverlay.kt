package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
internal fun MediaHeroScrimOverlay(modifier: Modifier = Modifier) {
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        background,
                        background.copy(alpha = 0.95f),
                        background.copy(alpha = 0.7f),
                        background.copy(alpha = 0.15f)
                    )
                )
            )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        background.copy(alpha = 0.05f),
                        background.copy(alpha = 0.2f),
                        background
                    )
                )
            )
    )
}
