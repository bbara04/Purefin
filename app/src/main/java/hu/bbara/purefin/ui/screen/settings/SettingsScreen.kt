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
import hu.bbara.purefin.core.feature.settings.SettingsViewModel
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.DropdownSetting
import hu.bbara.purefin.core.settings.RangeSetting
import hu.bbara.purefin.core.settings.SettingOption
import hu.bbara.purefin.core.settings.SettingsOptions
import hu.bbara.purefin.core.settings.StringSetting
import hu.bbara.purefin.core.settings.VoidSetting
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarIconButton
import hu.bbara.purefin.ui.screen.settings.components.BooleanSettingItem
import hu.bbara.purefin.ui.screen.settings.components.DropdownSettingItem
import hu.bbara.purefin.ui.screen.settings.components.RangeSettingItem
import hu.bbara.purefin.ui.screen.settings.components.StringSettingItem
import hu.bbara.purefin.ui.screen.settings.components.VoidSettingItem

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

            SettingsOptions.groups.forEach { group ->
                group.title?.let { title ->
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                group.options.forEach { option ->
                    item(key = option.key) {
                        SettingOptionItem(
                            option = option,
                            viewModel = viewModel
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingOptionItem(
    option: SettingOption<*>,
    viewModel: SettingsViewModel
) {
    when (option) {
        is RangeSetting -> {
            val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
            RangeSettingItem(
                title = option.title,
                value = value,
                valueRange = option.valueRange,
                onValueChange = { viewModel.set(option, it) }
            )
        }

        is BooleanSetting -> {
            val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
            BooleanSettingItem(
                title = option.title,
                value = value,
                onValueChange = { viewModel.set(option, it) }
            )
        }

        is StringSetting -> {
            val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
            StringSettingItem(
                title = option.title,
                value = value,
                onValueChange = { viewModel.set(option, it) }
            )
        }

        is VoidSetting -> {
            VoidSettingItem(
                title = option.title,
                onClick = {}
            )
        }

        is DropdownSetting<*> -> {
            DropdownSettingOptionItem(
                option = option,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun <T> DropdownSettingOptionItem(
    option: DropdownSetting<T>,
    viewModel: SettingsViewModel
) {
    val value by viewModel.value(option).collectAsState(initial = option.defaultValue)
    DropdownSettingItem(
        title = option.title,
        value = value,
        options = option.options,
        onValueChange = { viewModel.set(option, it) }
    )
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
