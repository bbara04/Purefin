package hu.bbara.purefin.core.feature.update

data class AppUpdateInfo(
    val versionCode: Long,
    val versionName: String?,
    val releaseNotes: String?,
    val apkUrl: String,
    val manifestUrl: String
)
