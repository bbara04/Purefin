package hu.bbara.purefin.core.download

sealed class DownloadState {
    data object NotDownloaded : DownloadState()
    data class Downloading(val progressPercent: Float) : DownloadState()
    data object Downloaded : DownloadState()
    data object Failed : DownloadState()
}
