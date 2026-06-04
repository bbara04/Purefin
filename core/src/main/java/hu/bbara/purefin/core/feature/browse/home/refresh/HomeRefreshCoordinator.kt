package hu.bbara.purefin.core.feature.browse.home.refresh

import hu.bbara.purefin.core.data.HomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class HomeRefreshCoordinator @Inject constructor(
    private val homeRepository: HomeRepository,
    private val sideEffects: Set<@JvmSuppressWildcards HomeRefreshSideEffect>,
) {
    private val sideEffectsMutex = Mutex()

    suspend fun onResumed() {
        refreshHomeData()
        runSideEffects()
    }

    suspend fun onRefresh(setRefreshing: (Boolean) -> Unit) {
        refreshHomeData(setRefreshing)
        runSideEffects()
    }

    private suspend fun refreshHomeData(
        setRefreshing: ((Boolean) -> Unit)? = null,
    ) {
        setRefreshing?.invoke(true)
        try {
            homeRepository.refreshHomeData()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Refresh is best-effort; don't crash on failure
        } finally {
            setRefreshing?.invoke(false)
        }
    }

    private suspend fun runSideEffects() {
        sideEffectsMutex.withLock {
            coroutineScope {
                sideEffects.forEach { sideEffect ->
                    launch {
                        try {
                            sideEffect.run()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) { }
                    }
                }
            }
        }
    }
}
