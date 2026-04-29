package hu.bbara.purefin.navigation

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LibraryDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
)
