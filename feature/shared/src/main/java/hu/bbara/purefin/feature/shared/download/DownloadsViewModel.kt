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
import kotlinx.coroutines.flow.combine
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
            MovieDto(
                id = movieId,
            )
        ))
    }

    fun onSeriesSelected(seriesId: UUID) {
        viewModelScope.launch {
            navigationManager.navigate(Route.SeriesRoute(
                SeriesDto(
                    id = seriesId,
                )
            ))
        }
    }

    val downloads = combine(
        offlineMediaRepository.movies,
        offlineMediaRepository.series
    ) { movies, series ->
        movies.values.map {
            PosterItem(
                type = BaseItemKind.MOVIE,
                movie = it
            )
        } + series.values.map {
            PosterItem(
                type = BaseItemKind.SERIES,
                series = it
            )
        }
    }
}