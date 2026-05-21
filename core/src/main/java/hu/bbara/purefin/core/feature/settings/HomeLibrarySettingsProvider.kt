package hu.bbara.purefin.core.feature.settings

import hu.bbara.purefin.core.data.HomeRepository
import hu.bbara.purefin.core.settings.BooleanSetting
import hu.bbara.purefin.core.settings.SettingGroup
import hu.bbara.purefin.core.settings.SettingsGroupProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeLibrarySettingsProvider @Inject constructor(
    private val homeRepository: HomeRepository,
) : SettingsGroupProvider {

    override val settingGroups: Flow<List<SettingGroup>> = homeRepository.libraries.map { libraries ->
        if (libraries.isEmpty()) return@map emptyList()

        val options = libraries.map { library ->
            BooleanSetting(
                key = "home_library_visible_${library.id}",
                title = library.name,
                defaultValue = true
            )
        }

        listOf(
            SettingGroup(
                title = "Home Screen Libraries",
                options = options
            )
        )
    }
}
