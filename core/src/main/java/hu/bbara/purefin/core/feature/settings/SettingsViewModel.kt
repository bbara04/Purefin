package hu.bbara.purefin.core.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.DropdownSetting
import hu.bbara.purefin.core.settings.RangeSetting
import hu.bbara.purefin.core.settings.SettingsRepository
import hu.bbara.purefin.core.settings.StringSetting
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

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

    fun onBack() {
        navigationManager.pop()
    }
}
