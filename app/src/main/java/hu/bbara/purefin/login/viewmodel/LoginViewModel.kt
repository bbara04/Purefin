package hu.bbara.purefin.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val jellyfinApiClient: JellyfinApiClient,
    @ApplicationContext private val currentContext: Context
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    val url: Flow<String> = userSessionRepository.session.map {
        it.url
    }

    suspend fun setUrl(url: String) {
        userSessionRepository.updateServerUrl(url)
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    suspend fun clearFields() {
        userSessionRepository.updateServerUrl("");
        _username.value = ""
        _password.value = ""
    }

    suspend fun login(): Boolean {
        return jellyfinApiClient.login(username.value, password.value)
    }

}
