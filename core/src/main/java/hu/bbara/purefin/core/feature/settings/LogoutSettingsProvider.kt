package hu.bbara.purefin.core.feature.settings

import hu.bbara.purefin.core.data.UserSessionRepository
import hu.bbara.purefin.core.settings.SettingGroup
import hu.bbara.purefin.core.settings.SettingsGroupProvider
import hu.bbara.purefin.core.settings.VoidSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutSettingsProvider @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) : SettingsGroupProvider {

    override val settingGroups: Flow<List<SettingGroup>> = flowOf(
        listOf(
            SettingGroup(
                title = "Account",
                options = listOf(
                    VoidSetting(
                        key = LOGOUT_KEY,
                        title = "Log out",
                        onClick = { userSessionRepository.logout() }
                    )
                )
            )
        )
    )

    private companion object {
        const val LOGOUT_KEY = "logout"
    }
}
