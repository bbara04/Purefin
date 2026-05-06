package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class LibraryDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    val name: String,
)
