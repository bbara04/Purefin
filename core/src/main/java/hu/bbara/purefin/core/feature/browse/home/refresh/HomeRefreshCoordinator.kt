package hu.bbara.purefin.core.feature.browse.home.refresh

import android.os.SystemClock
import hu.bbara.purefin.core.data.HomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// Singleton so the debounce window is shared across all ViewModel instances.
// A new coordinator per ViewModel would reset the timer on configuration
// changes and defeat the purpose of coalescing rapid resume/tab-switch
// events.
@Singleton
class HomeRefreshCoordinator @Inject constructor(
    private val homeRepository: HomeRepository,
    private val sideEffects: Set<@JvmSuppressWildcards HomeRefreshSideEffect>,
) {
    private val sideEffectsMutex = Mutex()

    // AtomicLong matches the rate-limit pattern used by the playback-position
    // side effect and is safe to read/write from any coroutine dispatcher.
    @OptIn(ExperimentalAtomicApi::class)
    private val lastRefreshAtMs = AtomicLong(0L)

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun onResumed() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRefreshAtMs.load() < RESUME_DEBOUNCE_MS) {
            Timber.tag(TAG).d("Skipping onResumed refresh (debounced)")
            return
        }
        refreshHomeData()
        runSideEffects()
        // Updated unconditionally on completion. refreshHomeData() catches
        // non-cancellation exceptions internally, so a failed refresh still
        // counts as "we just touched the server" and the next onResumed
        // within RESUME_DEBOUNCE_MS will be skipped. Pull-to-refresh always
        // bypasses this gate via onRefresh.
        lastRefreshAtMs.store(SystemClock.elapsedRealtime())
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun onRefresh(setRefreshing: (Boolean) -> Unit) {
        refreshHomeData(setRefreshing)
        runSideEffects()
        lastRefreshAtMs.store(SystemClock.elapsedRealtime())
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

    private companion object {
        const val TAG = "HomeRefreshCoordinator"
        // Coalesce rapid resume/tab-switch events into a single refresh.
        // Pull-to-refresh bypasses this gate via onRefresh.
        const val RESUME_DEBOUNCE_MS = 30_000L
    }
}
