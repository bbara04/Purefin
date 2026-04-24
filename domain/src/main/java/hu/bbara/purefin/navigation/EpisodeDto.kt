package hu.bbara.purefin.navigation

import java.util.UUID
import kotlinx.serialization.Serializable
import hu.bbara.purefin.navigation.UuidSerializer

@Serializable
data class EpisodeDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    @Serializable(with = UuidSerializer::class)
    val seasonId: UUID,
    @Serializable(with = UuidSerializer::class)
    val seriesId: UUID,
)
