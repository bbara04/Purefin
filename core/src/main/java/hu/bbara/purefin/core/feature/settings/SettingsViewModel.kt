package hu.bbara.purefin.core.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.NumberSetting
import hu.bbara.purefin.core.settings.SettingsRepository
import hu.bbara.purefin.core.settings.StringSetting
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val navigationManager: NavigationManager,
) : ViewModel() {

    fun value(option: NumberSetting) = settingsRepository.value(option)

    fun value(option: BooleanSetting) = settingsRepository.value(option)

    fun value(option: StringSetting) = settingsRepository.value(option)

    fun set(option: NumberSetting, value: Double) {
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

    fun onBack() {
        navigationManager.pop()
    }
}
