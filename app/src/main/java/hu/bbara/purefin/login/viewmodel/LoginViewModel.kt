package hu.bbara.purefin.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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

    val url: StateFlow<String> = userSessionRepository.session.map {
        it.url
    }.onStart { }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    suspend fun setUrl(url: String) {
        userSessionRepository.setServerUrl(url)
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    suspend fun clearFields() {
        userSessionRepository.setServerUrl("");
        _username.value = ""
        _password.value = ""
    }

    suspend fun login(): Boolean {
        return jellyfinApiClient.login(url.value, username.value, password.value)
    }

}
