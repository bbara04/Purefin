package hu.bbara.purefin.feature.update

interface AppVersionProvider {
    val versionCode: Long
    val updateManifestUrl: String
}
