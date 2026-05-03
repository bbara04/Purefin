package hu.bbara.purefin.feature.update

interface AppUpdateInstaller {
    suspend fun installUpdate(update: AppUpdateInfo): String
}
