package hu.bbara.purefin.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnit.Companion.Unspecified
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaSynopsis(
    synopsis: String,
    modifier: Modifier = Modifier,
    title: String = "Synopsis",
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    bodyColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    titleFontSize: TextUnit = 18.sp,
    bodyFontSize: TextUnit = 15.sp,
    bodyLineHeight: TextUnit? = 22.sp,
    titleSpacing: Dp = 12.dp,
    collapsedLines: Int = 3,
    collapseInitially: Boolean = true
) {
    var isExpanded by remember(synopsis) { mutableStateOf(!collapseInitially) }
    var isOverflowing by remember(synopsis) { mutableStateOf(false) }

    val containerModifier = if (isOverflowing) {
        modifier.clickable(role = Role.Button) { isExpanded = !isExpanded }
    } else {
        modifier
    }

    Column(modifier = containerModifier) {
        Text(
            text = title,
            color = titleColor,
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(titleSpacing))
        Text(
            text = synopsis,
            color = bodyColor,
            fontSize = bodyFontSize,
            lineHeight = bodyLineHeight ?: Unspecified,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedLines,
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                val overflowed = if (isExpanded) {
                    result.lineCount > collapsedLines
                } else {
                    result.hasVisualOverflow || result.lineCount > collapsedLines
                }
                if (overflowed != isOverflowing) {
                    isOverflowing = overflowed
                }
            }
        )
    }
}
