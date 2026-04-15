package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.Serializable
import hu.bbara.purefin.core.navigation.UuidSerializer

@Serializable
data class MovieDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
)
