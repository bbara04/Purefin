package hu.bbara.purefin.core.settings

sealed interface SettingOption<T> {
    val key: String
    val title: String
    val defaultValue: T
}

data class RangeSetting(
    override val key: String,
    override val title: String,
    override val defaultValue: Double,
    val valueRange: ClosedFloatingPointRange<Double> = 0.0..100.0,
) : SettingOption<Double>

data class BooleanSetting(
    override val key: String,
    override val title: String,
    override val defaultValue: Boolean
) : SettingOption<Boolean>

data class StringSetting(
    override val key: String,
    override val title: String,
    override val defaultValue: String
) : SettingOption<String>

data class ReadOnlySetting(
    override val key: String,
    override val title: String,
    val value: String
) : SettingOption<String> {
    override val defaultValue: String = value
}

data class VoidSetting(
    override val key: String,
    override val title: String,
    override val defaultValue: Unit = Unit,
    val onClick: suspend () -> Unit = {}
) : SettingOption<Unit>

data class DropdownSetting<T>(
    override val key: String,
    override val title: String,
    override val defaultValue: T,
    val options: List<T>
): SettingOption<T>

data class SettingGroup(
    val title: String?,
    val options: List<SettingOption<*>>
)

object SettingsOptions {

    val autoPlayNextMedia = BooleanSetting(
        key = "auto_play_next_media",
        title = "Autoplay next media",
        defaultValue = true
    )

    val groups = listOf(
        SettingGroup(
            title = "Playback",
            options = listOf(
                autoPlayNextMedia,
            )
        )
    )
}
