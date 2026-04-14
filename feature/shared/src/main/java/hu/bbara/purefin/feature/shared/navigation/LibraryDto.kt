package hu.bbara.purefin.feature.shared.navigation

import java.util.UUID
import kotlinx.serialization.Serializable
import org.jellyfin.sdk.model.serializer.UUIDSerializer

@Serializable
data class LibraryDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
)
