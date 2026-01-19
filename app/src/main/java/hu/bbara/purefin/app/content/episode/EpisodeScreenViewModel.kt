package hu.bbara.purefin.app.content.episode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.navigation.ItemDto
import hu.bbara.purefin.navigation.NavigationManager
import hu.bbara.purefin.navigation.Route
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ImageType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class EpisodeScreenViewModel @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val navigationManager: NavigationManager,
    private val userSessionRepository: UserSessionRepository
): ViewModel() {

    private val _episode = MutableStateFlow<EpisodeUiModel?>(null)
    val episode = _episode.asStateFlow()

    fun onSeriesSelected(seriesId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.Series(ItemDto(UUID.fromString(seriesId), BaseItemKind.SERIES)))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }


    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun selectNextUpEpisodeForSeries(seriesId: UUID) {
        viewModelScope.launch {
            val episode = jellyfinApiClient.getNextUpEpisode(seriesId)
            if (episode == null) {
                _episode.value = null
                return@launch
            }
            selectEpisodeInternal(episode.id)
        }
    }

    fun selectEpisode(episodeId: UUID) {
        viewModelScope.launch {
            selectEpisodeInternal(episodeId)
        }
    }

    private suspend fun selectEpisodeInternal(episodeId: UUID) {
        val episodeInfo = jellyfinApiClient.getItemInfo(episodeId)
        val serverUrl = userSessionRepository.serverUrl.first().trim().ifBlank {
            "https://jellyfin.bbara.hu"
        }
        _episode.value = episodeInfo!!.toUiModel(serverUrl)
    }

    private fun BaseItemDto.toUiModel(serverUrl: String): EpisodeUiModel {
        val releaseDate = formatReleaseDate(premiereDate, productionYear)
        val rating = officialRating ?: "NR"
        val runtime = formatRuntime(runTimeTicks)
        val format = container?.uppercase() ?: "VIDEO"
        val synopsis = overview ?: "No synopsis available."
        val heroImageUrl = id?.let { itemId ->
            JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = itemId,
                type = ImageType.PRIMARY
            )
        } ?: ""
        val cast = people.orEmpty().map { it.toCastMember() }
        return EpisodeUiModel(
            id = id,
            title = name ?: "Unknown title",
            releaseDate = releaseDate,
            rating = rating,
            runtime = runtime,
            format = format,
            synopsis = synopsis,
            heroImageUrl = heroImageUrl,
            audioTrack = "Default",
            subtitles = "Unknown",
            cast = cast
        )
    }

    private fun BaseItemPerson.toCastMember(): CastMember {
        return CastMember(
            name = name ?: "Unknown",
            role = role ?: "",
            imageUrl = null
        )
    }

    private fun formatReleaseDate(date: LocalDateTime?, fallbackYear: Int?): String {
        if (date == null) {
            return fallbackYear?.toString() ?: "—"
        }
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        return date.toLocalDate().format(formatter)
    }

    private fun formatRuntime(ticks: Long?): String {
        if (ticks == null || ticks <= 0) return "—"
        val totalSeconds = ticks / 10_000_000
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

}
