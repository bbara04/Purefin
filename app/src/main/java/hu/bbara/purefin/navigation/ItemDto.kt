package hu.bbara.purefin.navigation

import kotlinx.serialization.Serializable
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.serializer.UUIDSerializer

@Serializable
data class ItemDto (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val type : BaseItemKind
)