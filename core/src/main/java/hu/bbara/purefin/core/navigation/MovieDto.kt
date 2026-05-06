package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
)
