package hu.bbara.purefin.core.navigation

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
    private val commandsFlow = MutableSharedFlow<NavigationCommand>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val commands: SharedFlow<NavigationCommand> = commandsFlow.asSharedFlow()

    override fun navigate(route: Route) {
        commandsFlow.tryEmit(NavigationCommand.Navigate(route))
    }

    override fun replaceAll(route: Route) {
        commandsFlow.tryEmit(NavigationCommand.ReplaceAll(route))
    }

    override fun pop() {
        commandsFlow.tryEmit(NavigationCommand.Pop)
    }
}
