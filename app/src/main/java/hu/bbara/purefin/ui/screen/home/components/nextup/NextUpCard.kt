package hu.bbara.purefin.ui.screen.home.components.nextup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.common.media.homeMediaSharedBoundsSource
import hu.bbara.purefin.ui.common.media.rememberHomeMediaSharedBoundsClick
import hu.bbara.purefin.ui.model.MediaUiModel

@Composable
internal fun NextUpCard(
    uiModel: MediaUiModel,
    sharedBoundsKey: String,
    onMediaSelected: (MediaUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val onClick = rememberHomeMediaSharedBoundsClick(sharedBoundsKey) {
        onMediaSelected(uiModel)
    }

    MediaImageCard(
        imageUrl = uiModel.primaryImageUrl,
        title = uiModel.primaryText,
        subtitle = uiModel.secondaryText,
        onClick = onClick,
        imageModifier = Modifier.homeMediaSharedBoundsSource(sharedBoundsKey),
        shapeSize = 24.dp,
        titleStyle = MaterialTheme.typography.titleSmall,
        textPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = modifier.width(256.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.26f)
                        )
                    )
                )
        )
    }
}
