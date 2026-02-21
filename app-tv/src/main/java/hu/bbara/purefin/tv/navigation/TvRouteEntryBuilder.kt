package hu.bbara.purefin.tv.navigation

import androidx.navigation3.runtime.EntryProviderScope
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.login.ui.LoginScreen
import hu.bbara.purefin.tv.home.TvHomePage

fun EntryProviderScope<Route>.tvHomeSection() {
    entry<Route.Home> {
        TvHomePage()
    }
}

fun EntryProviderScope<Route>.tvLoginSection() {
    entry<Route.LoginRoute> {
        LoginScreen()
    }
}
