package hu.bbara.purefin.data.settings

import androidx.datastore.core.DataStore
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.DropdownSetting
import hu.bbara.purefin.core.settings.RangeSetting
import hu.bbara.purefin.core.settings.SettingsRepository
import hu.bbara.purefin.core.settings.StringSetting
import hu.bbara.purefin.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: DataStore<Settings>
) : SettingsRepository {

    override val settings: Flow<Settings> = settingsDataStore.data

    override fun value(option: RangeSetting): Flow<Double> {
        return settings
            .map { it.numberSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    override fun value(option: BooleanSetting): Flow<Boolean> {
        return settings
            .map { it.booleanSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    override fun value(option: StringSetting): Flow<String> {
        return settings
            .map { it.stringSettings[option.key] ?: option.defaultValue }
            .distinctUntilChanged()
    }

    override fun <T> value(option: DropdownSetting<T>): Flow<T> {
        return settings
            .map { settings ->
                val storedValue = settings.stringSettings[option.key]
                option.options.firstOrNull { it.toString() == storedValue } ?: option.defaultValue
            }
            .distinctUntilChanged()
    }

    override suspend fun set(option: RangeSetting, value: Double) {
        settingsDataStore.updateData { current ->
            current.copy(
                numberSettings = current.numberSettings + (option.key to value)
            )
        }
    }

    override suspend fun set(option: BooleanSetting, value: Boolean) {
        settingsDataStore.updateData { current ->
            current.copy(
                booleanSettings = current.booleanSettings + (option.key to value)
            )
        }
    }

    override suspend fun set(option: StringSetting, value: String) {
        settingsDataStore.updateData { current ->
            current.copy(
                stringSettings = current.stringSettings + (option.key to value)
            )
        }
    }

    override suspend fun <T> set(option: DropdownSetting<T>, value: T) {
        settingsDataStore.updateData { current ->
            current.copy(
                stringSettings = current.stringSettings + (option.key to value.toString())
            )
        }
    }
}
