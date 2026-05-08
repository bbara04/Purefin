package hu.bbara.purefin.core.settings

import hu.bbara.purefin.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    fun value(option: RangeSetting): Flow<Double>

    fun value(option: BooleanSetting): Flow<Boolean>

    fun value(option: StringSetting): Flow<String>

    fun <T> value(option: DropdownSetting<T>): Flow<T>

    suspend fun set(option: RangeSetting, value: Double)

    suspend fun set(option: BooleanSetting, value: Boolean)

    suspend fun set(option: StringSetting, value: String)

    suspend fun <T> set(option: DropdownSetting<T>, value: T)
}
