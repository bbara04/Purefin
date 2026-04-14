package hu.bbara.purefin.core.data.image

import java.util.UUID
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

        fun toPrefixImageUrl(url: String, itemId: UUID): String {
            if (url.isEmpty()) {
                return ""
            }
            return StringBuilder()
                .append(url)
                .append("/Items/")
                .append(itemId)
                .append("/Images/")
                .toString()
        }

        fun finishImageUrl(prefixImageUrl: String?, imageType: ImageType): String {
            if (prefixImageUrl.isNullOrEmpty()) {
                return ""
            }
            return StringBuilder()
                .append(prefixImageUrl)
                .append(imageType.serialName)
                .toString()

        }
    }
}