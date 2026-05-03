package hu.bbara.purefin.update

import hu.bbara.purefin.BuildConfig
import hu.bbara.purefin.feature.update.AppVersionProvider
import javax.inject.Inject

class BuildConfigAppVersionProvider @Inject constructor() : AppVersionProvider {
    override val versionCode: Long = BuildConfig.VERSION_CODE.toLong()
}
