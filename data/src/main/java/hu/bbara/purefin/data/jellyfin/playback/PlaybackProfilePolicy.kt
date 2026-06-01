package hu.bbara.purefin.data.jellyfin.playback

import org.jellyfin.sdk.model.api.DeviceProfile

interface PlaybackProfilePolicy {
    fun create(): DeviceProfile
}

internal class MobilePlaybackProfilePolicy(
    private val capabilities: DeviceProfileCapabilities,
) : PlaybackProfilePolicy {
    override fun create(): DeviceProfile =
        JellyfinAndroidMobileDeviceProfile.create(capabilities = capabilities)
}

internal class TvPlaybackProfilePolicy(
    private val capabilities: DeviceProfileCapabilities,
) : PlaybackProfilePolicy {
    override fun create(): DeviceProfile =
        JellyfinAndroidTvDeviceProfile.create(
            capabilities = capabilities
        )
}
