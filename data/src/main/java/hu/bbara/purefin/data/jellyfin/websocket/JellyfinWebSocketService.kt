package hu.bbara.purefin.data.jellyfin.websocket

import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import hu.bbara.purefin.model.Media
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
                    while (currentCoroutineContext().isActive) {
                        try {
                            listenForUserDataChanges()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (error: Exception) {
                            Timber.tag(TAG).w(error, "UserDataChanged subscription ended, retrying")
                        }
                        if (!currentCoroutineContext().isActive) break
                        delay(5000L)
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
                    val itemId = entry.itemId
                    localMediaRepository.getTypeById(itemId).let { media ->
                        when (media) {
                            is Media.MovieMedia -> {
                                localMediaRepository.loadMovie(media.id)
                            }
                            is Media.EpisodeMedia -> {
                                localMediaRepository.loadEpisode(media.id)
                            }
                            is Media.SeriesMedia -> {
                                localMediaRepository.loadSeries(media.id)
                            }
                            else -> {}
                        }
                    }
                }
            }
    }

    private companion object {
        const val TAG = "JellyfinWebSocket"
    }
}
