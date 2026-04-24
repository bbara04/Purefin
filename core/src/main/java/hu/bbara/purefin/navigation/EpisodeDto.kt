package hu.bbara.purefin.navigation

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EpisodeDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    @Serializable(with = UuidSerializer::class)
    val seasonId: UUID,
    @Serializable(with = UuidSerializer::class)
    val seriesId: UUID,
)
