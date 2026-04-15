package hu.bbara.purefin.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TvHomeMetaChipShape = RoundedCornerShape(999.dp)

@Composable
internal fun TvHomeMetaChip(
    text: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(TvHomeMetaChipShape)
            .background(
                if (highlighted) {
                    scheme.primary.copy(alpha = 0.16f)
                } else {
                    scheme.surfaceContainerHigh.copy(alpha = 0.82f)
                }
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (highlighted) scheme.primary else scheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
