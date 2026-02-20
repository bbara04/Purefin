package hu.bbara.purefin.app.home

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.core.data.navigation.Route

/**
 * Navigation 3 entry definition for the Home section.
 */
fun EntryProviderScope<Route>.homeSection() {
    entry<Route.Home> {
        HomePage()
    }
}
