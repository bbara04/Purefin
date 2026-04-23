package hu.bbara.purefin.core.image

import java.util.UUID

object ImageUrlBuilder {
    fun toImageUrl(url: String, itemId: UUID, artworkKind: ArtworkKind): String {
        if (url.isEmpty()) {
            return ""
        }
        return StringBuilder()
            .append(url)
            .append("/Items/")
            .append(itemId)
            .append("/Images/")
            .append(artworkKind.pathSegment)
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

    fun finishImageUrl(prefixImageUrl: String?, artworkKind: ArtworkKind): String {
        if (prefixImageUrl.isNullOrEmpty()) {
            return ""
        }
        return StringBuilder()
            .append(prefixImageUrl)
            .append(artworkKind.pathSegment)
            .toString()
    }
}
