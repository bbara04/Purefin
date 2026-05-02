package hu.bbara.purefin.ui.screen.libraries.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hu.bbara.purefin.ui.common.card.MediaImageCard
import hu.bbara.purefin.ui.model.LibraryUiModel

@Composable
fun LibraryListItem(
    uiModel: LibraryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MediaImageCard(
        imageUrl = uiModel.posterUrl,
        title = uiModel.name,
        onClick = onClick,
        modifier = modifier
    )
}
