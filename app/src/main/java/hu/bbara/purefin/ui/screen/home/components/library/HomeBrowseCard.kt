package hu.bbara.purefin.ui.screen.home.components.library

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bbara.purefin.ui.common.badge.WatchStateBadge
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.common.media.homeMediaSharedBoundsSource
import hu.bbara.purefin.ui.common.media.rememberHomeMediaSharedBoundsClick
import hu.bbara.purefin.ui.model.EpisodeUiModel
import hu.bbara.purefin.ui.model.MediaUiModel
import hu.bbara.purefin.ui.model.MovieUiModel

@Composable
internal fun HomeBrowseCard(
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
        imageAspectRatio = 15f / 16f,
        modifier = modifier.width(188.dp)
    ) {
        when (uiModel) {
            is MovieUiModel, is EpisodeUiModel -> {
                WatchStateBadge(
                    size = 28,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    watched = uiModel.watched,
                    started = (uiModel.progress ?: 0f) > 0f
                )
            }
            else -> Unit
        }
    }
}
