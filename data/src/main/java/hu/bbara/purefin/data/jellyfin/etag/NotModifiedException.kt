package hu.bbara.purefin.data.jellyfin.etag

/**
 * Thrown by [ETagInterceptor] when the Jellyfin server returns 304 Not Modified
 * for a request that had an `If-None-Match` header. Callers should treat this
 * as "the cached copy is still valid" and avoid re-processing the response.
 *
 * This is a control-flow signal, not an error — it propagates out of
 * `OkHttpClient.await()` before the SDK can deserialize a 304 body. Only
 * URLs that have been registered in [ETagCache] can produce this exception.
 */
class NotModifiedException : RuntimeException("ETag match — server returned 304 Not Modified")
