package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp

@Composable
fun MediaDetailsTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onCastClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    downFocusRequester: FocusRequester? = null
) {
    val downModifier = if (downFocusRequester != null) {
        Modifier.focusProperties { down = downFocusRequester }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GhostIconButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            modifier = downModifier
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostIconButton(
                icon = Icons.Outlined.Cast,
                contentDescription = "Cast",
                onClick = onCastClick,
                modifier = downModifier
            )
            GhostIconButton(
                icon = Icons.Outlined.MoreVert,
                contentDescription = "More",
                onClick = onMoreClick,
                modifier = downModifier
            )
        }
    }
}
