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

data class VoidSetting(
    override val key: String,
    override val title: String,
    override val defaultValue: Unit = Unit
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
    val defaultPlaybackSpeed = RangeSetting(
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

    val autoPlayNextMedia = BooleanSetting(
        key = "auto_play_next_media",
        title = "Autoplay next media",
        defaultValue = true
    )

    val preferredAudioLanguage = StringSetting(
        key = "preferred_audio_language",
        title = "Preferred audio language",
        defaultValue = "English"
    )

    val resetPlaybackSettings = VoidSetting(
        key = "reset_playback_settings",
        title = "Reset playback settings"
    )

    val streamingQuality = DropdownSetting(
        key = "streaming_quality",
        title = "Streaming quality",
        defaultValue = "Auto",
        options = listOf("Auto", "Low", "Medium", "High")
    )

    val groups = listOf(
        SettingGroup(
            title = "Playback",
            options = listOf(
                defaultPlaybackSpeed,
                confirmMobileDataPlayback,
                autoPlayNextMedia,
                preferredAudioLanguage,
                resetPlaybackSettings,
                streamingQuality
            )
        )
    )
}
