package hu.bbara.purefin.core.feature.update

interface AppUpdateInstaller {
    suspend fun installUpdate(update: AppUpdateInfo): String
}
