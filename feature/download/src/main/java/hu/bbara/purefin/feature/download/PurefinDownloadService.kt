package hu.bbara.purefin.feature.download

import android.app.Notification
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@OptIn(UnstableApi::class)
class PurefinDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_CHANNEL_ID,
    R.string.download_channel_name,
    R.string.download_channel_description
) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadServiceEntryPoint {
        fun downloadManager(): DownloadManager
        fun downloadNotificationHelper(): DownloadNotificationHelper
    }

    private val entryPoint: DownloadServiceEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, DownloadServiceEntryPoint::class.java)
    }

    private var lastBytesDownloaded: Long = 0L
    private var lastUpdateTimeMs: Long = 0L

    override fun getDownloadManager(): DownloadManager = entryPoint.downloadManager()

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        val activeDownloads = downloads.filter { it.state == Download.STATE_DOWNLOADING }

        if (activeDownloads.isEmpty()) {
            return entryPoint.downloadNotificationHelper().buildProgressNotification(
                this,
                R.drawable.ic_launcher_foreground,
                null,
                null,
                downloads,
                notMetRequirements
            )
        }

        val totalBytes = activeDownloads.sumOf { it.bytesDownloaded }
        val now = System.currentTimeMillis()

        val speedText = if (lastUpdateTimeMs > 0L) {
            val elapsed = (now - lastUpdateTimeMs).coerceAtLeast(1)
            val bytesPerSec = (totalBytes - lastBytesDownloaded) * 1000L / elapsed
            formatSpeed(bytesPerSec)
        } else {
            ""
        }

        lastBytesDownloaded = totalBytes
        lastUpdateTimeMs = now

        val percent = if (activeDownloads.size == 1) {
            activeDownloads[0].percentDownloaded
        } else {
            activeDownloads.map { it.percentDownloaded }.average().toFloat()
        }

        val title = if (activeDownloads.size == 1) {
            "Downloading"
        } else {
            "Downloading ${activeDownloads.size} files"
        }

        val contentText = buildString {
            append("${percent.toInt()}%")
            if (speedText.isNotEmpty()) {
                append(" · $speedText")
            }
        }

        return NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setProgress(100, percent.toInt(), false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun getScheduler(): Scheduler? = null

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return ""
        return when {
            bytesPerSec >= 1_000_000 -> String.format("%.1f MB/s", bytesPerSec / 1_000_000.0)
            bytesPerSec >= 1_000 -> String.format("%.0f KB/s", bytesPerSec / 1_000.0)
            else -> "$bytesPerSec B/s"
        }
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DOWNLOAD_CHANNEL_ID = "purefin_downloads"

        fun sendAddDownload(context: Context, request: DownloadRequest) {
            sendAddDownload(
                context,
                PurefinDownloadService::class.java,
                request,
                false
            )
        }

        fun sendRemoveDownload(context: Context, contentId: String) {
            sendRemoveDownload(
                context,
                PurefinDownloadService::class.java,
                contentId,
                false
            )
        }
    }
}
