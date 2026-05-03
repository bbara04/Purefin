package hu.bbara.purefin.feature.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class AppUpdateRepository @Inject constructor(
    private val appVersionProvider: AppVersionProvider
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkForUpdate(): AppUpdateInfo? {
        val manifestUrl = "http://purefin.t.bbara.hu/app/update.json"
        if (manifestUrl.isBlank()) {
            throw IllegalStateException("Update manifest URL not configured")
        }

        val manifest = withContext(Dispatchers.IO) { fetchManifest(manifestUrl) }
        if (manifest.versionCode <= appVersionProvider.versionCode) {
            return null
        }

        return AppUpdateInfo(
            versionCode = manifest.versionCode,
            versionName = manifest.versionName,
            releaseNotes = manifest.releaseNotes,
            apkUrl = manifest.apkUrl,
            manifestUrl = manifestUrl
        )
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
}

@Serializable
private data class AppUpdateManifest(
    val versionCode: Long,
    val versionName: String? = null,
    val apkUrl: String,
    val releaseNotes: String? = null
)
