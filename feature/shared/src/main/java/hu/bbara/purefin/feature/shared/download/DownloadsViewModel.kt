package hu.bbara.purefin.feature.shared.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.core.data.OfflineMediaRepository
import hu.bbara.purefin.core.data.navigation.MovieDto
import hu.bbara.purefin.core.data.navigation.NavigationManager
import hu.bbara.purefin.core.data.navigation.Route
import hu.bbara.purefin.core.data.navigation.SeriesDto
import hu.bbara.purefin.feature.download.MediaDownloadManager
import hu.bbara.purefin.feature.shared.home.PosterItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemKind
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val offlineMediaRepository: OfflineMediaRepository,
    private val navigationManager: NavigationManager,
    private val downloadManager: MediaDownloadManager
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
        offlineMediaRepository.movies,
        offlineMediaRepository.series,
        activeDownloadsMap
    ) { movies, series, inProgress ->
        movies.values
            .filter { it.id.toString() !in inProgress }
            .map { PosterItem(type = BaseItemKind.MOVIE, movie = it) } +
        series.values.map { PosterItem(type = BaseItemKind.SERIES, series = it) }
    }

    /** Items currently being downloaded with their progress. */
    val activeDownloads = combine(
        activeDownloadsMap,
        offlineMediaRepository.movies,
        offlineMediaRepository.episodes,
        offlineMediaRepository.series
    ) { inProgress, movies, episodes, seriesMap ->
        inProgress.mapNotNull { (contentId, progress) ->
            val id = try { UUID.fromString(contentId) } catch (e: Exception) { return@mapNotNull null }
            val movie = movies[id]
            if (movie != null) {
                ActiveDownloadItem(
                    contentId = contentId,
                    title = movie.title,
                    subtitle = "",
                    imageUrl = movie.heroImageUrl,
                    progress = progress
                )
            } else {
                val episode = episodes[id]
                episode?.let {
                    ActiveDownloadItem(
                        contentId = contentId,
                        title = it.title,
                        subtitle = seriesMap[it.seriesId]?.name ?: "",
                        imageUrl = it.heroImageUrl,
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
            if (offlineMediaRepository.episodes.value.containsKey(id)) {
                downloadManager.cancelEpisodeDownload(id)
            } else {
                downloadManager.cancelDownload(id)
            }
        }
    }
}
