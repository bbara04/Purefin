package hu.bbara.purefin.core.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coalesces concurrent and near-time-repeated suspend calls keyed by [key].
 *
 * Within a single [run] with a given [key]:
 *  - If a call with the same [key] is already in flight, the new caller
 *    awaits the same [Deferred] and receives the first caller's result.
 *  - If a call with the same [key] completed successfully less than
 *    [TTL_MS] ago, the cached result is returned without re-executing
 *    [block].
 *  - Otherwise [block] is executed, its result is cached for [TTL_MS]
 *    and shared with any concurrent awaiters.
 *
 * Failures are never cached: a thrown exception (other than
 * [CancellationException]) is propagated to all awaiters of the
 * in-flight call, but a subsequent call with the same key will execute
 *    [block] again immediately.
 *
 * [CancellationException] is always re-thrown so coroutine cancellation
 * semantics are preserved.
 */
@Singleton
class SingleFlight @Inject constructor() {
    private val inFlight: MutableMap<String, Deferred<*>> = ConcurrentHashMap()
    private val cache: MutableMap<String, CacheEntry<*>> = ConcurrentHashMap()
    private val mutex = Mutex()

    suspend fun <T> run(key: String, block: suspend () -> T): T {
        val now = System.currentTimeMillis()

        @Suppress("UNCHECKED_CAST")
        val cached = cache[key] as? CacheEntry<T>
        if (cached != null && now - cached.timestamp < TTL_MS) {
            return cached.value
        }

        val resolution: Resolution<T> = mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            val cachedNow = cache[key] as? CacheEntry<T>
            if (cachedNow != null && System.currentTimeMillis() - cachedNow.timestamp < TTL_MS) {
                return@withLock Resolution.Cached(cachedNow.value)
            }
            @Suppress("UNCHECKED_CAST")
            val existing = inFlight[key] as? Deferred<T>
            if (existing != null) {
                return@withLock Resolution.Join(existing)
            }
            val newDeferred = CompletableDeferred<T>()
            inFlight[key] = newDeferred
            Resolution.Start(newDeferred)
        }

        return when (resolution) {
            is Resolution.Cached -> resolution.value
            is Resolution.Join -> resolution.deferred.await()
            is Resolution.Start -> {
                val deferred = resolution.deferred
                try {
                    val result = block()
                    cache[key] = CacheEntry(result, System.currentTimeMillis())
                    deferred.complete(result)
                    result
                } catch (error: CancellationException) {
                    deferred.completeExceptionally(error)
                    throw error
                } catch (error: Throwable) {
                    deferred.completeExceptionally(error)
                    throw error
                } finally {
                    inFlight.remove(key, deferred)
                }
            }
        }
    }

    private sealed class Resolution<T> {
        data class Cached<T>(val value: T) : Resolution<T>()
        data class Join<T>(val deferred: Deferred<T>) : Resolution<T>()
        data class Start<T>(val deferred: CompletableDeferred<T>) : Resolution<T>()
    }

    private data class CacheEntry<T>(val value: T, val timestamp: Long)

    companion object {
        const val TTL_MS: Long = 15_000L
    }
}
