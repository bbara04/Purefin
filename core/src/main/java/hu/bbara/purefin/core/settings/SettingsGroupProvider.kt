package hu.bbara.purefin.core.settings

import kotlinx.coroutines.flow.Flow

interface SettingsGroupProvider {
    val settingGroups: Flow<List<SettingGroup>>
}