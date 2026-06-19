package hu.bbara.purefin.data.jellyfin.etag

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that turns Jellyfin's ETag support into a
 * "not modified" signal the SDK caller can act on.
 *
 * For URLs whose path is in [ETagCache.isEligible]:
 *   - If a cached ETag exists, the request is sent with `If-None-Match`.
 *   - If the server replies 304, the response is closed and a
 *     [NotModifiedException] is thrown so the caller can keep using its
 *     existing copy of the data.
 *   - If the server replies 2xx with an ETag header, the new ETag is cached.
 *
 * All other URLs are passed through untouched. This bounds the surface
 * area of the 304 handling: only code paths that explicitly know about
 * the home-refresh contract need to catch [NotModifiedException].
 */
@Singleton
class ETagInterceptor @Inject constructor(
    private val cache: ETagCache,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!cache.isEligible(request.url)) {
            return chain.proceed(request)
        }

        val url = request.url.toString()
        val etag = cache.get(url)
        val outgoing = if (etag != null) {
            request.newBuilder().header(HEADER_IF_NONE_MATCH, etag).build()
        } else {
            request
        }

        val response = chain.proceed(outgoing)

        if (response.code == HTTP_NOT_MODIFIED) {
            // Close before throwing so OkHttp does not warn about leaked
            // responses. The caller will treat this as "use the cached copy".
            response.close()
            Timber.tag(TAG).d("ETag hit for %s", url)
            throw NotModifiedException()
        }

        // Cache the ETag for next time. Only successful responses carry
        // meaningful ETags; error responses may carry caching hints but
        // we should not treat a 5xx body as a valid cache key.
        if (response.isSuccessful) {
            response.header(HEADER_ETAG)?.let { newEtag ->
                cache.put(url, newEtag)
            }
        }

        return response
    }

    private companion object {
        const val TAG = "ETagInterceptor"
        const val HEADER_ETAG = "ETag"
        const val HEADER_IF_NONE_MATCH = "If-None-Match"
        const val HTTP_NOT_MODIFIED = 304
    }
}
