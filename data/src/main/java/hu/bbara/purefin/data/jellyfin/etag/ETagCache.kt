package hu.bbara.purefin.data.jellyfin.etag

import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory store of `ETag` response headers, keyed by full request URL. The
 * [ETagInterceptor] reads from this store to add `If-None-Match` to outgoing
 * requests and writes to it on every successful response.
 *
 * ETag handling is scoped to a small set of home-refresh URL patterns
 * matched against path segments. All other endpoints (per-item lookups,
 * search, sessions, system info, etc.) are passed through unchanged. This
 * keeps the 304 → [NotModifiedException] translation from leaking into code
 * paths that do not expect it, and prevents search and similar list-shaped
 * calls from being treated as cacheable.
 *
 * The cache is process-scoped: ETags are lost on process death, which means
 * the first request after each app start always goes out without
 * `If-None-Match`. This is acceptable — the server returns the same data
 * with a fresh ETag, and subsequent requests benefit from the cache.
 */
@Singleton
class ETagCache @Inject constructor() {
    private val etags = ConcurrentHashMap<String, String>()

    /**
     * Returns `true` if requests to [url] should use ETag-based conditional
     * fetching. Only home-refresh endpoints are eligible, and the
     * `/Items` list endpoint is only eligible when it is being used as a
     * per-library content query (a `parentId` query parameter is present
     * and no search-shaped parameters are set).
     */
    fun isEligible(url: HttpUrl): Boolean = matchesHomeRefresh(url)

    /**
     * Returns the cached ETag for [url], or `null` if none has been stored
     * yet.
     */
    fun get(url: String): String? = etags[url]

    /**
     * Cache [etag] for [url]. The interceptor writes here after every
     * eligible 2xx response that carried an ETag header.
     */
    fun put(url: String, etag: String) {
        etags[url] = etag
    }

    private fun matchesHomeRefresh(url: HttpUrl): Boolean {
        val segments = url.pathSegments
        return when {
            // /Users/{userId}/Views — get libraries
            segments.size == 3 && segments[0] == "Users" && segments[2] == "Views" -> true
            // /Items/Suggestions — get suggestions
            segments == listOf("Items", "Suggestions") -> true
            // /Users/{userId}/Items/Resume — get resume items (continue watching)
            segments.size == 4 &&
                segments[0] == "Users" &&
                segments[2] == "Items" &&
                segments[3] == "Resume" -> true
            // /Shows/NextUp — get next up episodes
            segments == listOf("Shows", "NextUp") -> true
            // /Users/{userId}/Items/Latest — latest items in a library
            segments.size == 4 &&
                segments[0] == "Users" &&
                segments[2] == "Items" &&
                segments[3] == "Latest" -> true
            // /Items — per-library content list, distinguished from search
            // by the presence of `parentId` and the absence of search params.
            segments == listOf("Items") -> isPerLibraryItemsQuery(url)
            else -> false
        }
    }

    private fun isPerLibraryItemsQuery(url: HttpUrl): Boolean {
        // The per-library content call always sets parentId; the search
        // calls set searchTerm or genres but never parentId. Matching
        // on parentId alone is enough to keep the ETag scope tight.
        return url.queryParameter("parentId") != null
    }
}
