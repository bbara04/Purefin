package hu.bbara.purefin.core.data

import kotlinx.coroutines.flow.Flow

data class JellyfinServerCandidate(
    val name: String?,
    val address: String
)

data class QuickConnectSession(
    val code: String,
    val secret: String,
    val authenticated: Boolean
)

interface AuthenticationRepository {
    fun discoverServers(): Flow<JellyfinServerCandidate>
    suspend fun findServer(input: String): JellyfinServerCandidate?
    suspend fun isQuickConnectEnabled(url: String): Boolean
    suspend fun initiateQuickConnect(url: String): QuickConnectSession?
    suspend fun getQuickConnectState(url: String, secret: String): QuickConnectSession?
    suspend fun login(url: String, username: String, password: String): Boolean
    suspend fun loginWithQuickConnect(url: String, secret: String): Boolean
}
