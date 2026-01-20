package hu.bbara.purefin.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PurefinComplexTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = scheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = { Text(placeholder, color = scheme.onSurfaceVariant) },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = scheme.onSurfaceVariant) }
            } else null,
            trailingIcon = if (trailingIcon != null) {
                { Icon(Icons.Default.Visibility, contentDescription = null, tint = scheme.onSurfaceVariant) }
            } else null,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceVariant,
                unfocusedContainerColor = scheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = scheme.primary,
                focusedTextColor = scheme.onSurface,
                unfocusedTextColor = scheme.onSurface
            ))
    }
}
