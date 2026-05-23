package hu.bbara.purefin.ui.screen.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bbara.purefin.core.feature.settings.SettingsViewModel
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.DropdownSetting
import hu.bbara.purefin.core.settings.RangeSetting
import hu.bbara.purefin.core.settings.ReadOnlySetting
import hu.bbara.purefin.core.settings.SettingOption
import hu.bbara.purefin.core.settings.StringSetting
import hu.bbara.purefin.core.settings.VoidSetting
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBar
import hu.bbara.purefin.ui.screen.home.components.DefaultTopBarIconButton
import hu.bbara.purefin.ui.screen.settings.components.BooleanSettingItem
import hu.bbara.purefin.ui.screen.settings.components.DropdownSettingItem
import hu.bbara.purefin.ui.screen.settings.components.RangeSettingItem
import hu.bbara.purefin.ui.screen.settings.components.ReadOnlySettingItem
import hu.bbara.purefin.ui.screen.settings.components.StringSettingItem
import hu.bbara.purefin.ui.screen.settings.components.VoidSettingItem

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val settingGroups by viewModel.settingGroups.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(onBack = viewModel::onBack)
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            settingGroups.forEachIndexed { groupIndex, group ->
                group.title?.let { title ->
                    item(key = "${groupIndex}-title") {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                }

                itemsIndexed(
                    items = group.options,
                    key = { _, option -> option.key }
                ) { index, option ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = groupedSettingItemShape(
                            index = index,
                            itemCount = group.options.size
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingOptionItem(
                            option = option,
                            viewModel = viewModel
                        )
                    }
                }

                if (groupIndex < settingGroups.lastIndex) {
                    item(key = "${groupIndex}-spacer") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

private fun groupedSettingItemShape(
    index: Int,
    itemCount: Int
): RoundedCornerShape {
    val cornerRadius = 12.dp
    return when {
        itemCount == 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        index == itemCount - 1 -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )

        else -> RoundedCornerShape(0.dp)
    }
}

@Composable
private fun SettingOptionItem(
    option: SettingOption<*>,
    viewModel: SettingsViewModel
) {
    when (option) {
        is RangeSetting -> {
            val value by viewModel.value(option).collectAsStateWithLifecycle(initialValue = null)
            value?.let {
                RangeSettingItem(
                    title = option.title,
                    value = it,
                    valueRange = option.valueRange,
                    onValueChange = { value -> viewModel.set(option, value) }
                )
            }
        }

        is BooleanSetting -> {
            val value by viewModel.value(option).collectAsStateWithLifecycle(initialValue = null)
            value?.let {
                BooleanSettingItem(
                    title = option.title,
                    value = it,
                    onValueChange = { value -> viewModel.set(option, value) }
                )
            }
        }

        is StringSetting -> {
            val value by viewModel.value(option).collectAsStateWithLifecycle(initialValue = null)
            value?.let {
                StringSettingItem(
                    title = option.title,
                    value = it,
                    onValueChange = { value -> viewModel.set(option, value) }
                )
            }
        }

        is ReadOnlySetting -> {
            ReadOnlySettingItem(
                title = option.title,
                value = option.value
            )
        }

        is VoidSetting -> {
            VoidSettingItem(
                title = option.title,
                onClick = { viewModel.onClick(option) }
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
    val value by viewModel.value(option).collectAsStateWithLifecycle(initialValue = null)
    value?.let {
        DropdownSettingItem(
            title = option.title,
            value = it,
            options = option.options,
            onValueChange = { value -> viewModel.set(option, value) }
        )
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
