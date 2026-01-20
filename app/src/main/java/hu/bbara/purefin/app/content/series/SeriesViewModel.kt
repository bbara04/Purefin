package hu.bbara.purefin.app.content.series

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
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val navigationManager: NavigationManager,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _series = MutableStateFlow<SeriesUiModel?>(null)
    val series = _series.asStateFlow()

    fun onSelectEpisode(episodeId: String) {
        viewModelScope.launch {
            navigationManager.navigate(Route.Episode(ItemDto(id = UUID.fromString(episodeId), type = BaseItemKind.EPISODE)))
        }
    }

    fun onBack() {
        navigationManager.pop()
    }


    fun onGoHome() {
        navigationManager.replaceAll(Route.Home)
    }

    fun selectSeries(seriesId: UUID) {
        viewModelScope.launch {
            val serverUrl = userSessionRepository.serverUrl.first().trim().ifBlank {
                "https://jellyfin.bbara.hu"
            }
            val seriesItemResult = jellyfinApiClient.getItemInfo(mediaId = seriesId)
            val seasonsItemResult = jellyfinApiClient.getSeasons(seriesId)
            val episodesItemResult = seasonsItemResult.associate { season ->
                season.id to jellyfinApiClient.getEpisodesInSeason(seriesId, season.id)
            }
            val seriesUiModel = mapToSeriesUiModel(
                serverUrl,
                seriesItemResult,
                seasonsItemResult,
                episodesItemResult
            )
            _series.value = seriesUiModel
        }
    }

    private fun mapToSeriesUiModel(
        serverUrl: String,
        seriesItemResult: BaseItemDto?,
        seasonsItemResult: List<BaseItemDto>,
        episodesItemResult: Map<UUID, List<BaseItemDto>>
    ): SeriesUiModel {
        val seasonUiModels = seasonsItemResult.map { season ->
            val episodeItemResult = episodesItemResult[season.id] ?: emptyList()
            val episodeItemUiModels = episodeItemResult.map { episode ->
                SeriesEpisodeUiModel(
                    id = episode.id.toString(),
                    title = episode.name ?: "Unknown",
                    description = episode.overview ?: "",
                    duration = "58m",
                    imageUrl = JellyfinImageHelper.toImageUrl(url = serverUrl, itemId = episode.id, type = ImageType.BACKDROP)
                )
            }
            SeriesSeasonUiModel(
                name = season.name ?: "Unknown",
                episodes = episodeItemUiModels,
                // TODO add actual logic or remove
                isSelected = false,
            )
        }
        return SeriesUiModel(
            title = seriesItemResult?.name ?: "Unknown",
            format = seriesItemResult?.container ?: "VIDEO",
            rating = seriesItemResult?.officialRating ?: "NR",
            year = seriesItemResult!!.productionYear?.toString() ?: seriesItemResult!!.premiereDate?.year?.toString().orEmpty(),
            seasons = "3 Seasons",
            synopsis = seriesItemResult.overview ?: "No synopsis available.",
            heroImageUrl = JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = seriesItemResult.id,
                type = ImageType.BACKDROP
            ),
            seasonTabs = seasonUiModels,
            cast = seriesItemResult.people.orEmpty().map { it.toCastMember() }
        )
    }

    private fun BaseItemPerson.toCastMember(): SeriesCastMemberUiModel {
        return SeriesCastMemberUiModel(
            name = name ?: "Unknown",
            role = role ?: "",
            imageUrl = null
        )
    }

}