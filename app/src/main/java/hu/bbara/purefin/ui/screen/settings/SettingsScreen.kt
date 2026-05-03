package hu.bbara.purefin.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bbara.purefin.feature.settings.SettingsViewModel
import hu.bbara.purefin.settings.SettingsOptions
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarIconButton
import hu.bbara.purefin.ui.screen.settings.components.BooleanSettingItem
import hu.bbara.purefin.ui.screen.settings.components.NumberSettingItem
import hu.bbara.purefin.ui.screen.settings.components.StringSettingItem

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            SettingsTopBar(onBack = viewModel::onBack)
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            item {
                Text(
                    text = "Playback",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            SettingsOptions.numberSettings.forEach { option ->
                item(key = option.key) {
                    val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
                    NumberSettingItem(
                        title = option.title,
                        value = value,
                        valueRange = option.valueRange,
                        onValueChange = { viewModel.set(option, it) }
                    )
                    HorizontalDivider()
                }
            }

            SettingsOptions.booleanSettings.forEach { option ->
                item(key = option.key) {
                    val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
                    BooleanSettingItem(
                        title = option.title,
                        value = value,
                        onValueChange = { viewModel.set(option, it) }
                    )
                    HorizontalDivider()
                }
            }

            SettingsOptions.stringSettings.forEach { option ->
                item(key = option.key) {
                    val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
                    StringSettingItem(
                        title = option.title,
                        value = value,
                        onValueChange = { viewModel.set(option, it) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit
) {
    DefaultTopBar(
        leftActions = {
            DefaultTopBarIconButton(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
        }
    )
}
