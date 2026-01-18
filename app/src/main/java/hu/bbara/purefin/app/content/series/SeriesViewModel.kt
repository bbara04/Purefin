package hu.bbara.purefin.app.content.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bbara.purefin.client.JellyfinApiClient
import hu.bbara.purefin.image.JellyfinImageHelper
import hu.bbara.purefin.session.UserSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ImageType
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val jellyfinApiClient: JellyfinApiClient,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _series = MutableStateFlow<SeriesUiModel?>(null)
    val series = _series.asStateFlow()

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
            _series.value = mapToSeriesUiModel(serverUrl, seriesItemResult, seasonsItemResult, episodesItemResult)
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
                    title = episode.name ?: "Unknown",
                    description = episode.overview ?: "",
                    duration = "58m",
                    imageUrl = ""
                )
            }
            SeriesSeasonUiModel(
                name = season.name ?: "Unknown",
                episodes = episodeItemUiModels,
                isSelected = false,
            )
        }
        val heroImageUrl = seriesItemResult?.let { series ->
            JellyfinImageHelper.toImageUrl(
                url = serverUrl,
                itemId = series.id,
                type = ImageType.BACKDROP
            )
        } ?: ""
        return SeriesUiModel(
            title = seriesItemResult?.name ?: "Unknown",
            format = seriesItemResult?.container ?: "VIDEO",
            rating = seriesItemResult?.officialRating ?: "NR",
            year = seriesItemResult!!.productionYear?.toString() ?: seriesItemResult!!.premiereDate?.year?.toString().orEmpty(),
            seasons = "3 Seasons",
            synopsis = seriesItemResult.overview ?: "No synopsis available.",
            heroImageUrl = "",
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