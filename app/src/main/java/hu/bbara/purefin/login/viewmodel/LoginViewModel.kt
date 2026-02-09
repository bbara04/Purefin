package hu.bbara.purefin.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _url.value = userSessionRepository.serverUrl.first()
        }
    }

    fun setUrl(url: String) {
        _url.value = url
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun clearFields() {
        userSessionRepository.setServerUrl("");
        _username.value = ""
        _password.value = ""
    }

    suspend fun login(): Boolean {
        _errorMessage.value = null
        userSessionRepository.setServerUrl(url.value)
        val success = jellyfinApiClient.login(url.value, username.value, password.value)
        if (!success) {
            _errorMessage.value = "Login failed. Check your server URL, username, and password."
        }
        return success
    }

}
