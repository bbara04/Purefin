package hu.bbara.purefin.ui.screen.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BooleanSettingItem(
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = onValueChange
            )
        },
        modifier = modifier.clickable { onValueChange(!value) }
    )
}