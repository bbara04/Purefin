package hu.bbara.purefin.tv

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import hu.bbara.purefin.core.logging.PurefinLogger

@HiltAndroidApp
class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PurefinLogger.initialize(this)
    }
}
