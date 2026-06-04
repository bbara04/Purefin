package hu.bbara.purefin.core.feature.browse.home.refresh

import hu.bbara.purefin.core.download.MediaDownloadController
import javax.inject.Inject

class SyncSmartDownloadsHomeRefreshSideEffect @Inject constructor(
    private val mediaDownloadController: MediaDownloadController,
) : HomeRefreshSideEffect {

    override suspend fun run() {
        mediaDownloadController.syncSmartDownloads()
    }
}
