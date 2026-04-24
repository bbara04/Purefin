package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.Serializable
import hu.bbara.purefin.navigation.UuidSerializer

@Serializable
data class SeriesDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
)
