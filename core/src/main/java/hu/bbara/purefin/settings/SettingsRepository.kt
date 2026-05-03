package hu.bbara.purefin.settings

import androidx.datastore.core.DataStore
import hu.bbara.purefin.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsDataStore: DataStore<Settings>
) {
    val settings: Flow<Settings> = settingsDataStore.data

    fun value(option: NumberSetting): Flow<Double> {
        return settings
            .map { it.numberSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    fun value(option: BooleanSetting): Flow<Boolean> {
        return settings
            .map { it.booleanSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    fun value(option: StringSetting): Flow<String> {
        return settings
            .map { it.stringSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    suspend fun set(option: NumberSetting, value: Double) {
        settingsDataStore.updateData { current ->
            current.copy(
                numberSettings = current.numberSettings + (option.key to value)
            )
        }
    }

    suspend fun set(option: BooleanSetting, value: Boolean) {
        settingsDataStore.updateData { current ->
            current.copy(
                booleanSettings = current.booleanSettings + (option.key to value)
            )
        }
    }

    suspend fun set(option: StringSetting, value: String) {
        settingsDataStore.updateData { current ->
            current.copy(
                stringSettings = current.stringSettings + (option.key to value)
            )
        }
    }
}
