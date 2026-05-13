package hu.bbara.purefin.core.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.AuthenticationRepository
import hu.bbara.purefin.core.data.JellyfinServerCandidate
import hu.bbara.purefin.core.data.UserSessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel() {
    enum class Phase {
        SERVER_SEARCH,
        LOGIN
    }

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()
    private val _phase = MutableStateFlow(Phase.SERVER_SEARCH)
    val phase: StateFlow<Phase> = _phase.asStateFlow()
    private val _selectedServer = MutableStateFlow<JellyfinServerCandidate?>(null)
    val selectedServer: StateFlow<JellyfinServerCandidate?> = _selectedServer.asStateFlow()
    private val _discoveredServers = MutableStateFlow<List<JellyfinServerCandidate>>(emptyList())
    val discoveredServers: StateFlow<List<JellyfinServerCandidate>> = _discoveredServers.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()
    private val _quickConnectAvailable = MutableStateFlow(false)
    val quickConnectAvailable: StateFlow<Boolean> = _quickConnectAvailable.asStateFlow()
    private val _quickConnectCode = MutableStateFlow<String?>(null)
    val quickConnectCode: StateFlow<String?> = _quickConnectCode.asStateFlow()
    private val _isQuickConnecting = MutableStateFlow(false)
    val isQuickConnecting: StateFlow<Boolean> = _isQuickConnecting.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var quickConnectJob: Job? = null

    init {
        viewModelScope.launch {
            _url.value = userSessionRepository.serverUrl.first()
        }
        discoverServers()
    }

    fun setUrl(url: String) {
        clearError()
        _url.value = url
    }

    fun setUsername(username: String) {
        clearError()
        _username.value = username
    }

    fun setPassword(password: String) {
        clearError()
        _password.value = password
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun clearFields() {
        cancelQuickConnect()
        userSessionRepository.setServerUrl("")
        _url.value = ""
        _username.value = ""
        _password.value = ""
        _selectedServer.value = null
        _phase.value = Phase.SERVER_SEARCH
    }

    suspend fun login(): Boolean {
        cancelQuickConnect()
        _errorMessage.value = null
        _isLoggingIn.value = true
        return try {
            val serverUrl = selectedServer.value?.address ?: url.value
            userSessionRepository.setServerUrl(serverUrl)
            val success = authenticationRepository.login(serverUrl, username.value, password.value)
            if (!success) {
                _errorMessage.value = "Login failed. Check your server URL, username, and password."
            }
            success
        } finally {
            _isLoggingIn.value = false
        }
    }

    fun findServer() {
        viewModelScope.launch {
            val input = url.value.trim()
            if (input.isBlank()) {
                _errorMessage.value = "Enter a Jellyfin server address."
                return@launch
            }

            cancelQuickConnect()
            _isSearching.value = true
            _errorMessage.value = null
            val server = authenticationRepository.findServer(input)
            _isSearching.value = false
            if (server == null) {
                _errorMessage.value = "No Jellyfin server found for that address."
                return@launch
            }
            selectServer(server)
        }
    }

    fun selectDiscoveredServer(server: JellyfinServerCandidate) {
        viewModelScope.launch {
            cancelQuickConnect()
            _errorMessage.value = null
            selectServer(server)
        }
    }

    fun changeServer() {
        cancelQuickConnect()
        _errorMessage.value = null
        _quickConnectAvailable.value = false
        _selectedServer.value = null
        _phase.value = Phase.SERVER_SEARCH
    }

    fun startQuickConnect() {
        val serverUrl = selectedServer.value?.address ?: url.value
        if (serverUrl.isBlank()) {
            _errorMessage.value = "Select a Jellyfin server first."
            return
        }

        quickConnectJob?.cancel()
        quickConnectJob = viewModelScope.launch {
            _errorMessage.value = null
            _isQuickConnecting.value = true
            _quickConnectCode.value = null
            try {
                var session = authenticationRepository.initiateQuickConnect(serverUrl) ?: run {
                    _errorMessage.value = "Quick Connect is not available on this server."
                    return@launch
                }

                _quickConnectCode.value = session.code
                while (!session.authenticated) {
                    delay(2_000L)
                    val updatedSession = authenticationRepository.getQuickConnectState(serverUrl, session.secret)
                    if (updatedSession == null) {
                        _errorMessage.value = "Quick Connect failed. Try again or use manual login."
                        return@launch
                    }
                    session = updatedSession
                    _quickConnectCode.value = session.code
                }

                userSessionRepository.setServerUrl(serverUrl)
                val success = authenticationRepository.loginWithQuickConnect(serverUrl, session.secret)
                if (!success) {
                    _errorMessage.value = "Quick Connect login failed. Try again or use manual login."
                }
            } catch (_: CancellationException) {
            } finally {
                _isQuickConnecting.value = false
                _quickConnectCode.value = null
            }
        }
    }

    fun cancelQuickConnect() {
        quickConnectJob?.cancel()
        quickConnectJob = null
        _isQuickConnecting.value = false
        _quickConnectCode.value = null
    }

    override fun onCleared() {
        cancelQuickConnect()
        super.onCleared()
    }

    private fun discoverServers() {
        viewModelScope.launch {
            try {
                authenticationRepository.discoverServers().collect { server ->
                    _discoveredServers.value = (_discoveredServers.value + server)
                        .distinctBy { it.address }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun selectServer(server: JellyfinServerCandidate) {
        _selectedServer.value = server
        _url.value = server.address
        _phase.value = Phase.LOGIN
        userSessionRepository.setServerUrl(server.address)
        _quickConnectAvailable.value = authenticationRepository.isQuickConnectEnabled(server.address)
    }

}
