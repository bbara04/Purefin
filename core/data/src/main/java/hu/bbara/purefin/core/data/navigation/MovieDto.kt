package hu.bbara.purefin.core.data.navigation

import kotlinx.serialization.Serializable
import org.jellyfin.sdk.model.serializer.UUIDSerializer
import java.util.UUID

@Serializable
data class MovieDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
