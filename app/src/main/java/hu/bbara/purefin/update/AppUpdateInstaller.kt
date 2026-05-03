package hu.bbara.purefin.update

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import hu.bbara.purefin.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class AppUpdateInstaller(
    context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdateAndInstall(): String {
        val manifestUrl = "http://purefin.t.bbara.hu/app/update.json"
        if (manifestUrl.isBlank()) {
            return "Update manifest URL not configured"
        }

        val manifest = withContext(Dispatchers.IO) { fetchManifest(manifestUrl) }
        if (manifest.versionCode <= BuildConfig.VERSION_CODE.toLong()) {
            return "Purefin is up to date"
        }

        if (!appContext.packageManager.canRequestPackageInstalls()) {
            openInstallPermissionSettings()
            return "Allow app installs, then check again"
        }

        val apkFile = withContext(Dispatchers.IO) {
            downloadApk(manifestUrl, manifest)
                .also { validateApk(it, manifest.versionCode) }
        }

        withContext(Dispatchers.IO) {
            commitInstallSession(apkFile, manifest.versionCode)
        }

        val versionLabel = manifest.versionName?.takeIf { it.isNotBlank() } ?: manifest.versionCode.toString()
        return "Downloaded Purefin $versionLabel"
    }

    private fun fetchManifest(manifestUrl: String): AppUpdateManifest {
        val request = Request.Builder()
            .url(manifestUrl)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Update check failed: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Update manifest empty")
            return json.decodeFromString<AppUpdateManifest>(body)
        }
    }

    private fun downloadApk(manifestUrl: String, manifest: AppUpdateManifest): File {
        val apkUrl = manifestUrl.toHttpUrl().resolve(manifest.apkUrl)
            ?: throw IllegalStateException("APK URL invalid")
        val request = Request.Builder()
            .url(apkUrl)
            .build()
        val updateDir = appContext.cacheDir.resolve("updates")
        updateDir.mkdirs()
        updateDir.listFiles()?.forEach { it.delete() }
        val apkFile = File(updateDir, "purefin-${manifest.versionCode}.apk")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("APK download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("APK download empty")
            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return apkFile
    }

    private fun validateApk(apkFile: File, expectedVersionCode: Long) {
        val packageInfo = appContext.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            ?: throw IllegalStateException("Downloaded file is not a valid APK")
//        if (packageInfo.packageName != appContext.packageName) {
//            throw IllegalStateException("Downloaded APK package does not match Purefin")
//        }
//        if (packageInfo.longVersionCode != expectedVersionCode) {
//            throw IllegalStateException("Downloaded APK version does not match update manifest")
//        }
//        if (packageInfo.longVersionCode <= BuildConfig.VERSION_CODE.toLong()) {
//            throw IllegalStateException("Downloaded APK is not newer")
//        }
    }

    private fun commitInstallSession(apkFile: File, versionCode: Long) {
        val installer = appContext.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(appContext.packageName)
            setInstallReason(PackageManager.INSTALL_REASON_USER)
            setSize(apkFile.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE)
            }
        }
        val sessionId = installer.createSession(params)
        try {
            installer.openSession(sessionId).use { session ->
                apkFile.inputStream().use { input ->
                    session.openWrite("purefin-$versionCode.apk", 0, apkFile.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                session.commit(statusReceiver(sessionId))
            }
        } catch (e: Exception) {
            installer.abandonSession(sessionId)
            throw e
        }
    }

    private fun statusReceiver(sessionId: Int): IntentSender {
        val intent = Intent(appContext, AppUpdateInstallReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return pendingIntent.intentSender
    }

    private fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            appContext.startActivity(
                Intent(Settings.ACTION_SECURITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

@Serializable
private data class AppUpdateManifest(
    val versionCode: Long,
    val versionName: String? = null,
    val apkUrl: String
)
