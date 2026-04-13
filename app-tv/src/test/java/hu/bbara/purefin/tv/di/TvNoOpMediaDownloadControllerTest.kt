package hu.bbara.purefin.tv.di

import hu.bbara.purefin.core.data.download.DownloadState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TvNoOpMediaDownloadControllerTest {

    @Test
    fun `tv no-op download controller always reports no downloads`() = runBlocking {
        assertEquals(emptyMap<String, Float>(), TvNoOpMediaDownloadController.observeActiveDownloads().first())
        assertEquals(
            DownloadState.NotDownloaded,
            TvNoOpMediaDownloadController.observeDownloadState("movie-1").value
        )
    }
}
