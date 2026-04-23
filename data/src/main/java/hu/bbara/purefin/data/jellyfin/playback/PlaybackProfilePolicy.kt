package hu.bbara.purefin.data.jellyfin.playback

import org.jellyfin.sdk.model.ServerVersion
import org.jellyfin.sdk.model.api.DeviceProfile

interface PlaybackProfilePolicy {
    fun create(serverVersion: ServerVersion): DeviceProfile
}

internal object PlaybackProfileDefaults {
    val fallbackServerVersion = ServerVersion(10, 10, 0)
}

internal class MobilePlaybackProfilePolicy(
    private val capabilities: DeviceProfileCapabilities,
) : PlaybackProfilePolicy {
    override fun create(serverVersion: ServerVersion): DeviceProfile =
        JellyfinAndroidMobileDeviceProfile.create(capabilities = capabilities)
}

internal class TvPlaybackProfilePolicy(
    private val capabilities: DeviceProfileCapabilities,
) : PlaybackProfilePolicy {
    override fun create(serverVersion: ServerVersion): DeviceProfile =
        JellyfinAndroidTvDeviceProfile.create(
            capabilities = capabilities,
            serverVersion = serverVersion,
        )
}
