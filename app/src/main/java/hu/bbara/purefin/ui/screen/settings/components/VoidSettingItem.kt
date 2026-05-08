package hu.bbara.purefin.ui.screen.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VoidSettingItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}
