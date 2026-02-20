package hu.bbara.purefin.core.data.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface NavigationCommand {
    data class Navigate(val route: Route) : NavigationCommand
    data class ReplaceAll(val route: Route) : NavigationCommand
    data object Pop : NavigationCommand
}

interface NavigationManager {
    val commands: SharedFlow<NavigationCommand>
    fun navigate(route: Route)
    fun replaceAll(route: Route)
    fun pop()
}

class DefaultNavigationManager : NavigationManager {
    private val _commands =
        MutableSharedFlow<NavigationCommand>(
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    override val commands: SharedFlow<NavigationCommand> = _commands.asSharedFlow()

    override fun navigate(route: Route) {
        _commands.tryEmit(NavigationCommand.Navigate(route))
    }

    override fun replaceAll(route: Route) {
        _commands.tryEmit(NavigationCommand.ReplaceAll(route))
    }

    override fun pop() {
        _commands.tryEmit(NavigationCommand.Pop)
    }
}

val LocalNavigationManager: ProvidableCompositionLocal<NavigationManager> =
    staticCompositionLocalOf { error("NavigationManager not provided") }
