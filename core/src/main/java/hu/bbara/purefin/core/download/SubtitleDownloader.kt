package hu.bbara.purefin.core.download

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.core.data.OfflineMediaManager
import hu.bbara.purefin.model.DownloadedSubtitle
import hu.bbara.purefin.model.ExternalSubtitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads external (sidecar) subtitle files to app-local storage and records
 * their metadata via [OfflineMediaManager] so they can be attached to offline
 * media items during playback.
 *
 * Subtitle downloads are best-effort: a failure for one subtitle is logged and
 * skipped so that a missing subtitle never blocks a media download.
 */
@OptIn(UnstableApi::class)
@Singleton
class SubtitleDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val offlineMediaManager: OfflineMediaManager,
) {

    suspend fun downloadAndStore(mediaId: UUID, subtitles: List<ExternalSubtitle>) {
        if (subtitles.isEmpty()) {
            offlineMediaManager.deleteSubtitles(mediaId)
            return
        }
        withContext(Dispatchers.IO) {
            val mediaDir = File(context.filesDir, "subtitles/${mediaId}").apply {
                if (!exists()) mkdirs()
            }

            val downloaded = mutableListOf<DownloadedSubtitle>()
            for (sub in subtitles) {
                val ext = extensionForMimeType(sub.mimeType) ?: run {
                    Timber.tag(TAG).w("No file extension for subtitle mime type ${sub.mimeType}; skipping")
                    continue
                }
                val target = File(mediaDir, "${sub.index}.$ext")

                val ok = runCatching { downloadFile(sub.remoteUrl, target) }
                    .onFailure { Timber.tag(TAG).e(it, "Failed to download subtitle ${sub.index} for $mediaId") }
                    .getOrDefault(false)

                if (!ok) continue

                downloaded += DownloadedSubtitle(
                    index = sub.index,
                    language = sub.language,
                    label = sub.label,
                    mimeType = sub.mimeType,
                    forced = sub.forced,
                    defaultTrack = sub.defaultTrack,
                    localFilePath = target.absolutePath,
                )
            }

            offlineMediaManager.saveSubtitles(mediaId, downloaded)
            Timber.tag(TAG).d("Stored ${downloaded.size}/${subtitles.size} subtitles for $mediaId")
        }
    }

    suspend fun deleteForMedia(mediaId: UUID) {
        withContext(Dispatchers.IO) {
            offlineMediaManager.deleteSubtitles(mediaId)
            val mediaDir = File(context.filesDir, "subtitles/${mediaId}")
            if (mediaDir.exists()) {
                mediaDir.deleteRecursively()
            }
        }
    }

    private fun downloadFile(url: String, target: File): Boolean {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Timber.tag(TAG).w("Subtitle download failed (${response.code}): $url")
                return false
            }
            response.body.byteStream().use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return true
        }
    }

    private fun extensionForMimeType(mimeType: String): String? = when (mimeType) {
        MimeTypes.APPLICATION_SUBRIP -> "srt"
        MimeTypes.TEXT_VTT -> "vtt"
        MimeTypes.APPLICATION_TTML -> "ttml"
        MimeTypes.TEXT_SSA -> "ssa"
        else -> null
    }

    private companion object {
        private const val TAG = "SubtitleDownloader"
    }
}