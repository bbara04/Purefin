package hu.bbara.purefin.data.logging

import hu.bbara.purefin.core.logging.PurefinLogger
import hu.bbara.purefin.core.settings.SettingGroup
import hu.bbara.purefin.core.settings.SettingsGroupProvider
import hu.bbara.purefin.core.settings.VoidSetting
import hu.bbara.purefin.data.jellyfin.client.JellyfinApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogUploadSettingsProvider @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
) : SettingsGroupProvider {
    private val uploadMutex = Mutex()

    override val settingGroups: Flow<List<SettingGroup>> = flowOf(
        listOf(
            SettingGroup(
                title = "Logs",
                options = listOf(
                    VoidSetting(
                        key = UPLOAD_LOGS_KEY,
                        title = "Upload logs",
                        onClick = { uploadLogs() }
                    )
                )
            )
        )
    )

    private suspend fun uploadLogs() {
        uploadMutex.withLock {
            val logFiles = PurefinLogger.prepareFilesForUpload()
            if (logFiles.isEmpty()) {
                Timber.tag(TAG).d("No log files to upload")
                return
            }

            logFiles.forEach { logFile ->
                val uploadedName = jellyfinApiClient.uploadLogFile(logFile.data)
                    ?: error("Log upload failed")
                PurefinLogger.deleteUploadedFile(logFile)
                Timber.tag(TAG).d("Uploaded log file ${logFile.name} as $uploadedName and deleted it locally")
            }
        }
    }

    private companion object {
        const val TAG = "LogUploadSettings"
        const val UPLOAD_LOGS_KEY = "upload_logs"
    }
}
