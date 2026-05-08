package hu.bbara.purefin.core.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.DropdownSetting
import hu.bbara.purefin.core.settings.RangeSetting
import hu.bbara.purefin.core.settings.SettingGroup
import hu.bbara.purefin.core.settings.SettingsGroupProvider
import hu.bbara.purefin.core.settings.SettingsOptions
import hu.bbara.purefin.core.settings.SettingsRepository
import hu.bbara.purefin.core.settings.StringSetting
import hu.bbara.purefin.core.settings.VoidSetting
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val navigationManager: NavigationManager,
    settingsGroupProviders: Set<@JvmSuppressWildcards SettingsGroupProvider>,
) : ViewModel() {

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private val dynamicSettingGroups = settingsGroupProviders
        .map { it.settingGroups }
        .takeIf { it.isNotEmpty() }
        ?.let { groupFlows ->
            combine(groupFlows) { groups -> groups.toList().flatten() }
        }
        ?: flowOf(emptyList())

    val settingGroups: StateFlow<List<SettingGroup>> = dynamicSettingGroups
        .combine(flowOf(SettingsOptions.groups)) { dynamicGroups, staticGroups ->
            staticGroups + dynamicGroups
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsOptions.groups
        )

    fun value(option: RangeSetting) = settingsRepository.value(option)

    fun value(option: BooleanSetting) = settingsRepository.value(option)

    fun value(option: StringSetting) = settingsRepository.value(option)

    fun <T> value(option: DropdownSetting<T>) = settingsRepository.value(option)

    fun set(option: RangeSetting, value: Double) {
        viewModelScope.launch {
            settingsRepository.set(option, value)
        }
    }

    fun set(option: BooleanSetting, value: Boolean) {
        viewModelScope.launch {
            settingsRepository.set(option, value)
        }
    }

    fun set(option: StringSetting, value: String) {
        viewModelScope.launch {
            settingsRepository.set(option, value)
        }
    }

    fun <T> set(option: DropdownSetting<T>, value: T) {
        viewModelScope.launch {
            settingsRepository.set(option, value)
        }
    }

    fun onClick(option: VoidSetting) {
        viewModelScope.launch {
            try {
                option.onClick()
            } catch (e: Exception) {
                _snackbarMessages.emit(e.message ?: "Setting action failed")
            }
        }
    }

    fun onBack() {
        navigationManager.pop()
    }
}
