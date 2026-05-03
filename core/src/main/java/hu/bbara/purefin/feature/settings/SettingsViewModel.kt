package hu.bbara.purefin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.settings.BooleanSetting
import hu.bbara.purefin.settings.NumberSetting
import hu.bbara.purefin.settings.SettingsRepository
import hu.bbara.purefin.settings.StringSetting
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
