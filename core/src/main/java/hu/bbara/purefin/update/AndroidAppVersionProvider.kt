package hu.bbara.purefin.update

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.bbara.purefin.feature.update.AppVersionProvider
import javax.inject.Inject

class AndroidAppVersionProvider @Inject constructor(
    @ApplicationContext context: Context
) : AppVersionProvider {
    private val appContext = context.applicationContext

    override val versionCode: Long
        get() = appContext.packageManager.currentPackageInfo().longVersionCode

    override val updateManifestUrl: String
        get() = appContext.packageManager.currentApplicationInfo()
            .metaData
            ?.getString(UPDATE_MANIFEST_URL_META_DATA)
            .orEmpty()

    private fun PackageManager.currentPackageInfo(): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(appContext.packageName, 0)
        }

    private fun PackageManager.currentApplicationInfo(): ApplicationInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplicationInfo(
                appContext.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            getApplicationInfo(appContext.packageName, PackageManager.GET_META_DATA)
        }

    private companion object {
        const val UPDATE_MANIFEST_URL_META_DATA = "hu.bbara.purefin.UPDATE_MANIFEST_URL"
    }
}
