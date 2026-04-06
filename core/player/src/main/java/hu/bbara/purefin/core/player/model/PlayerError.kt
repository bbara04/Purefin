package hu.bbara.purefin.core.player.model

import androidx.media3.common.PlaybackException

enum class PlayerErrorSource {
    LOAD,
    PLAYBACK
}

data class PlayerError(
    val summary: String,
    val technicalDetail: String? = null,
    val source: PlayerErrorSource,
    val errorCode: Int? = null,
    val errorCodeName: String? = null,
    val retryable: Boolean = false
) {
    val detailText: String?
        get() = technicalDetail?.takeIf { detail ->
            detail.isNotBlank() && !detail.equals(summary, ignoreCase = true)
        }

    fun withAdditionalTechnicalDetail(additionalDetail: String?): PlayerError {
        val normalizedAdditionalDetail = additionalDetail?.trim()?.takeIf { it.isNotEmpty() } ?: return this
        val mergedDetail = linkedSetOf<String>().apply {
            technicalDetail?.trim()?.takeIf { it.isNotEmpty() }?.let(::add)
            add(normalizedAdditionalDetail)
        }.joinToString(" | ")

        return copy(technicalDetail = mergedDetail)
    }

    companion object {
        fun loadFailure(
            summary: String = "Unable to load media",
            technicalDetail: String? = null,
            retryable: Boolean = true
        ): PlayerError {
            return PlayerError(
                summary = summary,
                technicalDetail = technicalDetail,
                source = PlayerErrorSource.LOAD,
                retryable = retryable
            )
        }

        fun invalidMediaId(mediaId: String): PlayerError {
            return PlayerError(
                summary = "Invalid media id",
                technicalDetail = "The requested media id is not a valid UUID: $mediaId",
                source = PlayerErrorSource.LOAD,
                retryable = false
            )
        }

        fun fromPlaybackException(error: PlaybackException): PlayerError {
            return playbackFailure(
                errorCode = error.errorCode,
                errorCodeName = error.errorCodeName,
                technicalDetail = error.localizedMessage,
                cause = error.cause
            )
        }

        fun playbackFailure(
            errorCode: Int? = null,
            errorCodeName: String? = null,
            technicalDetail: String? = null,
            cause: Throwable? = null,
            retryable: Boolean = true
        ): PlayerError {
            val mergedTechnicalDetail = linkedSetOf<String>().apply {
                errorCodeName?.takeIf { it.isNotBlank() }?.let(::add)
                technicalDetail?.takeIf { it.isNotBlank() }?.let(::add)
                cause?.toTechnicalDetail()?.let(::add)
            }.joinToString(" | ").ifBlank { null }

            return PlayerError(
                summary = "Playback error",
                technicalDetail = mergedTechnicalDetail,
                source = PlayerErrorSource.PLAYBACK,
                errorCode = errorCode,
                errorCodeName = errorCodeName,
                retryable = retryable
            )
        }

        fun fromThrowable(
            throwable: Throwable,
            summary: String = "Unable to load media",
            retryable: Boolean = true
        ): PlayerError {
            return loadFailure(
                summary = summary,
                technicalDetail = throwable.toTechnicalDetail(),
                retryable = retryable
            )
        }

        internal fun Throwable.toTechnicalDetail(): String {
            val typeName = this::class.simpleName ?: javaClass.simpleName.ifBlank { javaClass.name }
            val message = message?.trim().takeIf { !it.isNullOrEmpty() }
            return if (message != null) {
                "$typeName: $message"
            } else {
                typeName
            }
        }
    }
}
