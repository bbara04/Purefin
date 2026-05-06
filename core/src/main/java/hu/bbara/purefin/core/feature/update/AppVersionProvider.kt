package hu.bbara.purefin.core.feature.update

interface AppVersionProvider {
    val versionCode: Long
    val updateManifestUrl: String
}
