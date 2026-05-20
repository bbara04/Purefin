package hu.bbara.purefin.core.data

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>

    fun reportRequestSucceeded()

    fun reportRequestFailed(error: Throwable)
}
