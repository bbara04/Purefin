package hu.bbara.purefin.feature.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
    private val appUpdateInstaller: AppUpdateInstaller
) : ViewModel() {

    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var isInstallingUpdate = false

    fun checkForUpdates(showUpToDateMessage: Boolean = true) {
        if (_isCheckingForUpdates.value) {
            return
        }

        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            try {
                val update = appUpdateRepository.checkForUpdate()
                if (update == null) {
                    if (showUpToDateMessage) {
                        _snackbarMessages.emit("Purefin is up to date")
                    }
                } else {
                    _availableUpdate.value = update
                }
            } catch (e: Exception) {
                _snackbarMessages.emit(e.message ?: "Update check failed")
            } finally {
                _isCheckingForUpdates.value = false
            }
        }
    }

    fun acceptUpdate() {
        val update = _availableUpdate.value ?: return
        if (isInstallingUpdate) {
            return
        }

        viewModelScope.launch {
            isInstallingUpdate = true
            _availableUpdate.value = null
            try {
                _snackbarMessages.emit(appUpdateInstaller.installUpdate(update))
            } catch (e: Exception) {
                _snackbarMessages.emit(e.message ?: "Update install failed")
            } finally {
                isInstallingUpdate = false
            }
        }
    }

    fun declineUpdate() {
        _availableUpdate.value = null
    }
}
