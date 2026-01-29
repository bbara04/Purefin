package hu.bbara.purefin.data.model

import java.util.UUID

data class Series(
    val id: UUID,
    val name: String,
    val synopsis: String,
    val year: String,
    val heroImageUrl: String,
    val seasonCount: Int,
    val seasons: List<Season>
)
