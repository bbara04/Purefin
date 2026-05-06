package hu.bbara.purefin.core.download

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadModuleTest {

    @Test
    fun `download cache keeps no-op evictor`() {
        assertTrue(DownloadModuleFactories.createDownloadCacheEvictor() is NoOpCacheEvictor)
    }

    @Test
    fun `phone playback data source reads download cache and writes upstream only`() {
        val upstreamFactory = FakeDataSourceFactory()
        val factory = DownloadModuleFactories.newPhonePlaybackDataSourceFactory(upstreamFactory)

        assertTrue(readPrivateBoolean(factory, "cacheIsReadOnly"))
        assertNull(readPrivateField(factory, "cacheWriteDataSinkFactory"))
        assertSame(upstreamFactory, readPrivateField(factory, "upstreamDataSourceFactory"))
    }

    private fun readPrivateBoolean(instance: CacheDataSource.Factory, fieldName: String): Boolean {
        return readPrivateField(instance, fieldName) as Boolean
    }

    private fun readPrivateField(instance: CacheDataSource.Factory, fieldName: String): Any? {
        val field = instance.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance)
    }
}

private class FakeDataSourceFactory : DataSource.Factory {
    override fun createDataSource(): DataSource {
        throw UnsupportedOperationException("Unused in unit tests")
    }
}
