package hu.bbara.purefin.common.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    backgroundColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    cursorColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val resolvedBackgroundColor =
        if (backgroundColor == Color.Unspecified) scheme.surfaceVariant else backgroundColor
    val resolvedTextColor = if (textColor == Color.Unspecified) scheme.onSurface else textColor
    val resolvedCursorColor = if (cursorColor == Color.Unspecified) scheme.primary else cursorColor

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = resolvedBackgroundColor,
            unfocusedContainerColor = resolvedBackgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = resolvedCursorColor,
            focusedTextColor = resolvedTextColor,
            unfocusedTextColor = resolvedTextColor,
            focusedLeadingIconColor = scheme.onSurfaceVariant,
            unfocusedLeadingIconColor = scheme.onSurfaceVariant,
            focusedPlaceholderColor = scheme.onSurfaceVariant,
            unfocusedPlaceholderColor = scheme.onSurfaceVariant,
        ))
}
