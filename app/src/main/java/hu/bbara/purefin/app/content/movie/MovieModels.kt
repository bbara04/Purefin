package hu.bbara.purefin.app.content.movie

data class CastMember(
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class MovieUiModel(
    val title: String,
    val year: String,
    val rating: String,
    val runtime: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val audioTrack: String,
    val subtitles: String,
    val cast: List<CastMember>
)
