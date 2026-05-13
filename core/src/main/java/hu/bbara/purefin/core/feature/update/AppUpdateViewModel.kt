package hu.bbara.purefin.core.feature.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val appUpdateController: AppUpdateController
) : ViewModel() {

    val isCheckingForUpdates: StateFlow<Boolean> = appUpdateController.isCheckingForUpdates

    val availableUpdate: StateFlow<AppUpdateInfo?> = appUpdateController.availableUpdate

    val snackbarMessages: SharedFlow<String> = appUpdateController.snackbarMessages

    fun checkForUpdates(showUpToDateMessage: Boolean = true) {
        viewModelScope.launch {
            appUpdateController.checkForUpdates(showUpToDateMessage = showUpToDateMessage)
        }
    }

    fun checkForUpdatesOnAppOpen() {
        viewModelScope.launch {
            appUpdateController.checkForUpdatesOnAppOpen()
        }
    }

    fun acceptUpdate() {
        viewModelScope.launch {
            appUpdateController.installAvailableUpdate()
        }
    }

    fun declineUpdate() {
        appUpdateController.declineUpdate()
    }
}
