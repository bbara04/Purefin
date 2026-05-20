package hu.bbara.purefin.data.jellyfin

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NetworkMonitor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val isServerReachable = MutableStateFlow(true)

    private val isDeviceOnline: Flow<Boolean> = callbackFlow {
        var wasDeviceOnline = connectivityManager.isCurrentlyConnected()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isCurrentlyConnected = connectivityManager.isCurrentlyConnected()
                if (isCurrentlyConnected && !wasDeviceOnline) {
                    isServerReachable.value = true
                }
                wasDeviceOnline = isCurrentlyConnected
                trySend(isCurrentlyConnected)
            }

            override fun onLost(network: Network) {
                val isCurrentlyConnected = connectivityManager.isCurrentlyConnected()
                wasDeviceOnline = isCurrentlyConnected
                trySend(isCurrentlyConnected)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        trySend(wasDeviceOnline)
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    override val isOnline: StateFlow<Boolean> = combine(
        isDeviceOnline,
        isServerReachable
    ) { isDeviceOnline, isServerReachable ->
        isDeviceOnline && isServerReachable
    }.distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = connectivityManager.isCurrentlyConnected() && isServerReachable.value
        )

    override fun reportRequestSucceeded() {
        isServerReachable.value = true
    }

    override fun reportRequestFailed(error: Throwable) {
        if (error.isConnectivityFailure()) {
            isServerReachable.value = false
        }
    }

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
        val network = activeNetwork ?: return false
        val caps = getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun Throwable.isConnectivityFailure(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            when (current) {
                is UnknownHostException,
                is ConnectException,
                is NoRouteToHostException,
                is SocketTimeoutException -> return true
            }
            current = current.cause
        }
        return false
    }
}
