package hu.bbara.purefin.settings

sealed interface SettingOption<T> {
    val key: String
    val title: String
    val defaultValue: T
}

data class NumberSetting(
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

object SettingsOptions {
    val defaultPlaybackSpeed = NumberSetting(
        key = "default_playback_speed",
        title = "Default playback speed",
        defaultValue = 1.0,
        valueRange = 0.5..2.0
    )

    val confirmMobileDataPlayback = BooleanSetting(
        key = "confirm_mobile_data_playback",
        title = "Confirm mobile data playback",
        defaultValue = true
    )

    val preferredAudioLanguage = StringSetting(
        key = "preferred_audio_language",
        title = "Preferred audio language",
        defaultValue = "English"
    )

    val numberSettings = listOf(defaultPlaybackSpeed)
    val booleanSettings = listOf(confirmMobileDataPlayback)
    val stringSettings = listOf(preferredAudioLanguage)
}
