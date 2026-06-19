package hu.bbara.purefin.core.jellyfin

import javax.inject.Qualifier

/**
 * Marks an `OkHttpClient` that is wired into the Jellyfin SDK's `OkHttpFactory`.
 * Distinct from the unqualified client used for image loading and media streaming
 * because the SDK has its own auth path and the ETag interceptor should not leak
 * into non-SDK HTTP calls.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JellyfinSdkClient
