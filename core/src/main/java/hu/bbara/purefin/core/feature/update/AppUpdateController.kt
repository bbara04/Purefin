package hu.bbara.purefin.core.feature.update

import hu.bbara.purefin.core.settings.ReadOnlySetting
import hu.bbara.purefin.core.settings.SettingGroup
import hu.bbara.purefin.core.settings.SettingOption
import hu.bbara.purefin.core.settings.SettingsGroupProvider
import hu.bbara.purefin.core.settings.VoidSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateController @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val appVersionProvider: AppVersionProvider
) : SettingsGroupProvider {
    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private val checkMutex = Mutex()
    private val installMutex = Mutex()

    override val settingGroups: Flow<List<SettingGroup>> = availableUpdate
        .map { update ->
            val options = listOfNotNull<SettingOption<*>>(
                buildNumberSetting(),
                update?.let { installUpdateSetting(it) }
            )

            listOf(
                SettingGroup(
                    title = "App",
                    options = options
                )
            )
        }

    suspend fun checkForUpdates(
        showUpToDateMessage: Boolean = true,
        showFailureMessage: Boolean = true
    ) {
        if (!checkMutex.tryLock()) {
            return
        }

        _isCheckingForUpdates.value = true
        try {
            val update = appUpdateRepository.checkForUpdate()
            _availableUpdate.value = update
            if (update == null && showUpToDateMessage) {
                _snackbarMessages.emit("Purefin is up to date")
            }
        } catch (e: Exception) {
            if (showFailureMessage) {
                _snackbarMessages.emit(e.message ?: "Update check failed")
            }
        } finally {
            _isCheckingForUpdates.value = false
            checkMutex.unlock()
        }
    }

    suspend fun installAvailableUpdate() {
        val update = _availableUpdate.value ?: return
        if (!installMutex.tryLock()) {
            return
        }

        _availableUpdate.value = null
        try {
            _snackbarMessages.emit(appUpdateInstaller.installUpdate(update))
        } catch (e: Exception) {
            _snackbarMessages.emit(e.message ?: "Update install failed")
        } finally {
            installMutex.unlock()
        }
    }

    fun declineUpdate() {
        _availableUpdate.value = null
    }

    private fun installUpdateSetting(update: AppUpdateInfo): VoidSetting {
        val versionLabel = update.versionName?.takeIf { it.isNotBlank() } ?: update.versionCode.toString()
        return VoidSetting(
            key = INSTALL_APP_UPDATE_KEY,
            title = "Install Purefin $versionLabel",
            onClick = { installAvailableUpdate() }
        )
    }

    private fun buildNumberSetting() = ReadOnlySetting(
        key = BUILD_NUMBER_KEY,
        title = "Build number",
        value = appVersionProvider.versionCode.toString()
    )

    private companion object {
        const val BUILD_NUMBER_KEY = "build_number"
        const val INSTALL_APP_UPDATE_KEY = "install_app_update"
    }
}
