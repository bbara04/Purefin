package hu.bbara.purefin.core.settings

import hu.bbara.purefin.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    fun value(option: NumberSetting): Flow<Double>

    fun value(option: BooleanSetting): Flow<Boolean>

    fun value(option: StringSetting): Flow<String>

    suspend fun set(option: NumberSetting, value: Double)

    suspend fun set(option: BooleanSetting, value: Boolean)

    suspend fun set(option: StringSetting, value: String)
}
