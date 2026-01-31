package hu.bbara.purefin.navigation

import kotlinx.serialization.Serializable
import org.jellyfin.sdk.model.serializer.UUIDSerializer
import java.util.UUID

@Serializable
data class EpisodeDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val seasonId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val seriesId: UUID,
)
