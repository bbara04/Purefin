package hu.bbara.purefin

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import hu.bbara.purefin.core.logging.PurefinLogger

@HiltAndroidApp
class PurefinApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PurefinLogger.initialize(this)
    }
}
