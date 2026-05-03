package hu.bbara.purefin.settings

sealed interface SettingOption<T> {
    val key: String
    val defaultValue: T
}

data class NumberSetting(
    override val key: String,
    override val defaultValue: Double
) : SettingOption<Double>

data class BooleanSetting(
    override val key: String,
    override val defaultValue: Boolean
) : SettingOption<Boolean>

data class StringSetting(
    override val key: String,
    override val defaultValue: String
) : SettingOption<String>
