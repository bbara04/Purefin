package hu.bbara.purefin.data.jellyfin

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.NetworkMonitor
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jellyfinApiClient: JellyfinApiClient,
) : NetworkMonitor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val isAndroidConnected = MutableStateFlow(connectivityManager.isCurrentlyConnected())
    private val isServerReachable = MutableStateFlow(true)

    override val isOnline: Flow<Boolean> = combine(
        isAndroidConnected,
        isServerReachable,
    ) { androidConnected, serverReachable ->
        androidConnected && serverReachable
    }.distinctUntilChanged()

    init {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isAndroidConnected.value = true
                scope.launch { updateServerReachability() }
            }

            override fun onLost(network: Network) {
                isAndroidConnected.value = connectivityManager.isCurrentlyConnected()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
    }

    override suspend fun checkConnection(): Boolean {
        val androidConnected = connectivityManager.isCurrentlyConnected()
        isAndroidConnected.value = androidConnected
        if (!androidConnected) {
            isServerReachable.value = false
            return false
        }
        return updateServerReachability()
    }

    private suspend fun updateServerReachability(): Boolean {
        val reachable = try {
            withTimeoutOrNull(SERVER_CHECK_TIMEOUT_MS) {
                jellyfinApiClient.probeServer()
            } ?: false
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag(TAG).w(error, "Jellyfin server reachability check failed")
            false
        }
        isServerReachable.value = reachable
        return reachable
    }

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
        val network = activeNetwork ?: return false
        val caps = getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        private const val TAG = "ConnectivityNetworkMonitor"
        private const val SERVER_CHECK_TIMEOUT_MS = 5_000L
    }
}
