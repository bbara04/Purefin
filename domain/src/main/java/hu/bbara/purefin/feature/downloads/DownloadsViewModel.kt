package hu.bbara.purefin.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.OfflineCatalogReader
import hu.bbara.purefin.core.download.MediaDownloadController
import hu.bbara.purefin.core.image.ArtworkKind
import hu.bbara.purefin.core.image.ImageUrlBuilder
import hu.bbara.purefin.core.navigation.MovieDto
import hu.bbara.purefin.core.navigation.NavigationManager
import hu.bbara.purefin.core.navigation.Route
import hu.bbara.purefin.core.navigation.SeriesDto
import hu.bbara.purefin.core.ui.model.MovieUiModel
import hu.bbara.purefin.core.ui.model.SeriesUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val offlineCatalogReader: OfflineCatalogReader,
    private val navigationManager: NavigationManager,
    private val downloadManager: MediaDownloadController,
) : ViewModel() {

    fun onMovieSelected(movieId: UUID) {
        navigationManager.navigate(Route.MovieRoute(
            MovieDto(id = movieId)
        ))
    }

    fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(id = seriesId)
            ))
        }
    }

    // Shared polling source: contentId → progress (0–100f). Starts when UI is subscribed.
    private val activeDownloadsMap = downloadManager.observeActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Items that are fully downloaded and not currently in progress. */
    val downloads = combine(
        offlineCatalogReader.movies,
        offlineCatalogReader.series,
        activeDownloadsMap
    ) { movies, series, inProgress ->
        movies.values
            .filter { it.id.toString() !in inProgress }
            .map { MovieUiModel(it) } +
        series.values.map { SeriesUiModel(it) }
    }

    /** Items currently being downloaded with their progress. */
    val activeDownloads = combine(
        activeDownloadsMap,
        offlineCatalogReader.movies,
        offlineCatalogReader.episodes,
        offlineCatalogReader.series
    ) { inProgress, movies, episodes, seriesMap ->
        inProgress.mapNotNull { (contentId, progress) ->
            val id = try { UUID.fromString(contentId) } catch (e: Exception) { return@mapNotNull null }
            val movie = movies[id]
            if (movie != null) {
                ActiveDownloadItem(
                    contentId = contentId,
                    title = movie.title,
                    subtitle = "",
                    imageUrl = ImageUrlBuilder.finishImageUrl(movie.imageUrlPrefix, ArtworkKind.PRIMARY),
                    progress = progress
                )
            } else {
                val episode = episodes[id]
                episode?.let {
                    ActiveDownloadItem(
                        contentId = contentId,
                        title = it.title,
                        subtitle = seriesMap[it.seriesId]?.name ?: "",
                        imageUrl = ImageUrlBuilder.finishImageUrl(it.imageUrlPrefix, ArtworkKind.PRIMARY),
                        progress = progress
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancelDownload(contentId: String) {
        viewModelScope.launch {
            val id = try {
                UUID.fromString(contentId)
            } catch (e: Exception) {
                return@launch
            }
            if (offlineCatalogReader.episodes.value.containsKey(id)) {
                downloadManager.cancelEpisodeDownload(id)
            } else {
                downloadManager.cancelDownload(id)
            }
        }
    }
}
