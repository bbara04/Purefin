package hu.bbara.purefin.core.data.image

import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.ImageType

class JellyfinImageHelper {
    companion object {
        fun toImageUrl(url: String, itemId: UUID, type: ImageType): String {
            if (url.isEmpty()) {
                return ""
            }
            return StringBuilder()
                .append(url)
                .append("/Items/")
                .append(itemId)
                .append("/Images/")
                .append(type.serialName)
                .toString()
        }
    }
}