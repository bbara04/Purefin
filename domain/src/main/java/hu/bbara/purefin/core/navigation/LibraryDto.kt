package hu.bbara.purefin.core.navigation

import java.util.UUID
import kotlinx.serialization.Serializable
import hu.bbara.purefin.core.navigation.UuidSerializer

@Serializable
data class LibraryDto(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    val name: String,
)
