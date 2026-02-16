package hu.bbara.purefin.data.local.room

import javax.inject.Qualifier

/**
 * Qualifier for online database and its components.
 * Used for the primary MediaDatabase that syncs with the Jellyfin server.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnlineDatabase

/**
 * Qualifier for offline database and its components.
 * Used for the OfflineMediaDatabase that stores downloaded content for offline viewing.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineDatabase

/**
 * Qualifier for the online media repository.
 * Provides access to media synced from the Jellyfin server.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnlineRepository

/**
 * Qualifier for the offline media repository.
 * Provides access to media downloaded for offline viewing.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineRepository
