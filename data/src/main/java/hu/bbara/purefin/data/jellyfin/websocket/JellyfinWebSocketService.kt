package hu.bbara.purefin.data.jellyfin.websocket

import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jellyfin.sdk.api.sockets.subscribe
import org.jellyfin.sdk.model.api.UserDataChangedMessage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Subscribes to the Jellyfin server's [UserDataChangedMessage] WebSocket event
 * and propagates the changes to the local media repository so the UI updates
 * the affected item in place.
 *
 * The WebSocket follows the lifecycle of the [JellyfinApiClient]'s [ApiClient][org.jellyfin.sdk.api.client.ApiClient]:
 * the SDK automatically reconnects when the server URL, client info, device
 * info or access token change (see [org.jellyfin.sdk.api.sockets.SocketApi]).
 *
 * The subscription is only active while the user is logged in and the device
 * is online, matching the gating used by other long-running background work
 * such as [hu.bbara.purefin.data.jellyfin.playback.SyncPlaybackPositionsHomeRefreshSideEffect].
 */
@Singleton
class JellyfinWebSocketService @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository,
    private val networkMonitor: NetworkMonitor,
    private val localMediaRepository: LocalMediaRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            combine(
                userSessionRepository.isLoggedIn,
                networkMonitor.isOnline,
            ) { loggedIn, online -> loggedIn && online }
                .distinctUntilChanged()
                // collectLatest cancels the previous body on every new active
                // value, so a logout / connectivity flap really tears down the
                // in-flight subscription instead of leaving it running.
                .collectLatest { active ->
                    if (!active) return@collectLatest
                    // Re-subscribe in a loop so a transient failure (e.g. the
                    // first subscription attempt throws because the access
                    // token is still being applied) does not permanently
                    // silence the service.
                    while (currentCoroutineContext().isActive) {
                        try {
                            listenForUserDataChanges()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (error: Exception) {
                            Timber.tag(TAG).w(error, "UserDataChanged subscription ended, retrying")
                        }
                        if (!currentCoroutineContext().isActive) break
                        delay(RETRY_DELAY_MS)
                    }
                }
        }
    }

    private suspend fun listenForUserDataChanges() {
        jellyfinApiClient.api.webSocket
            .subscribe<UserDataChangedMessage>()
            .collect { message ->
                val info = message.data ?: return@collect
                for (entry in info.userDataList) {
                    val itemId = entry.itemId ?: continue
                    // updateWatchProgressPercent sets both `progress` and a
                    // percent-derived `watched` (>= 90 % rule, see
                    // InMemoryLocalMediaRepository.updateWatchProgressPercent).
                    // The follow-up markAsWatched is needed so the server's
                    // explicit `played` flag wins over the percent-derived
                    // boolean when they disagree (e.g. a stale "100 %" entry
                    // with played = false on an unmark), and so we set
                    // `watched = true` for entries that only carry `played`.
                    entry.playedPercentage
                        ?.takeUnless { it.isNaN() }
                        ?.let { localMediaRepository.updateWatchProgressPercent(itemId, it) }
                    localMediaRepository.markAsWatched(itemId, entry.played)
                }
            }
    }

    private companion object {
        const val TAG = "JellyfinWebSocket"
        const val RETRY_DELAY_MS = 5_000L
    }
}
