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
    backgroundColor: Color,
    textColor: Color,
    cursorColor: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp)),
        placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
        leadingIcon =
            { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = scheme.onSurfaceVariant) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = cursorColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
        ))
}