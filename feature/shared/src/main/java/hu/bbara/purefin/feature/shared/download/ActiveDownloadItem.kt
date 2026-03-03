package hu.bbara.purefin.feature.shared.download

data class ActiveDownloadItem(
    val contentId: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val progress: Float,
)
