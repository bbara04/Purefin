package hu.bbara.purefin.app.content.series

data class SeriesEpisodeUiModel(
    val title: String,
    val description: String,
    val duration: String,
    val imageUrl: String
)

data class SeriesSeasonUiModel(
    val name: String,
    val isSelected: Boolean,
    val episodes: List<SeriesEpisodeUiModel>
)

data class SeriesCastMemberUiModel(
    val name: String,
    val role: String,
    val imageUrl: String?
)

data class SeriesUiModel(
    val title: String,
    val year: String,
    val rating: String,
    val seasons: String,
    val format: String,
    val synopsis: String,
    val heroImageUrl: String,
    val seasonTabs: List<SeriesSeasonUiModel>,
    val cast: List<SeriesCastMemberUiModel>
)

internal object SeriesMockData {
    fun series(): SeriesUiModel {
        val heroUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD3hBjDpw00tDCQsK5xNcnJra301k1T4LksWVZzHieH9KHQItEQkVzhwevJvf8RkaQKdVKvObzRlfDDqa3_PNwLUlUQc1LpDih8p94VTGobEV62qi7QrmNyQm_o55KRMNWiTG3zLLpblGqo3uUNQcYmPFqfNML95dClXQ4lQNl85-zgerPPAbGPr23dswbIYCigyTAaXgrmdV_nbNQ5LdDB0Wh5cMHtP0uxz6k3ARjNom6clhphGIUF9e6YSvKuwuiZ-1lMYFg8C_4"
        val episode1 = SeriesEpisodeUiModel(
            title = "E1: The Beginning",
            description = "The crew assembles for the first time as the anomaly begins to expand rapidly near Saturn's rings.",
            duration = "58m",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6OPszCXCIP_FMO3BJJUrjpCtDNw9aeHYOGyOAXdqF078hDFNrH7KXbaQ7qtipz6aIPLivd8VBBffNMbeAiYIjjWjn5GMb6Xn9iiJz0D2rzhCKi0TBeFrN6tC1IXJkzQyQKJNhTnyokWy9dd-YtN65V7er7RT6hP5jdVBXhtK1xZMjlgrm1bk_FTTmKd8Afu3zPtJCaaC98Z608vav5zhYlkrdA1wKNSTWTpzwMSyDIY3pNQNPFauWf0n-iEu7QsYTAwhCG_zfxz0"
        )
        val episode2 = SeriesEpisodeUiModel(
            title = "E2: Event Horizon",
            description = "Dr. Cole discovers a frequency embedded in the rift's radiation that suggests intelligent design.",
            duration = "54m",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBExsf-wEzAVjMxasU2ImGhlreqQo9biBSN1yHyAbW8MyuhuppRw9ho7OD3vsbySSJ3kNluEgH1Qun45PmLnZWixZsFU4Qc7UGGJNKMS5Nkm4GZAsKdFvb3z_i1tkCvaXXvGpqmwI0qjFuo1QyjjhYPA5Yp3I8ZhrnDYdQv_GxbhR6Vl3mY1rbxd2BIUEE5oMTwTF-QmJztUEaViZkSGSG2VgVXZ5VAREn4xWE902OH2sysllvXQJQIaj439JIC2_Vg61m0-F-F1Vc"
        )
        val episode3 = SeriesEpisodeUiModel(
            title = "E3: Singularity",
            description = "Tension rises as the ship approaches the event horizon, and the AI begins to behave erratically.",
            duration = "1h 02m",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA5CFDWsWYO4YxdRoLd2QfH5Su2KLhtj5xSDb8qmzWHvPE888ac_HAAj1wu1uqdFNSncdmmJ-bWsc--h6NYKxVXkhd4vHaFWi0XTJXgsR0F3cBu_l2SynSX4TMNSy5C3XWDurgeSH789byOe1HvoxHCHTJYaSf3OyEbil-NOp9g_9mZ24CIZOI79nx57CRzmooxoswycqssPpfTNkrnoYrrAczt5qbncwLM9NVU442YxyBFisr2Ds9H-CNBOakiCtaKnoJ6npznM7U"
        )
        return SeriesUiModel(
            title = "Interstellar Horizon: The Series",
            year = "2024",
            rating = "TV-MA",
            seasons = "3 Seasons",
            format = "4K HDR",
            synopsis = "When a mysterious cosmic rift appears near Saturn, a team of seasoned astronauts and theoretical physicists must embark on a high-stakes voyage across dimensions. They seek to unlock the secrets of time-dilated anomalies that threaten the very fabric of human existence on Earth.",
            heroImageUrl = heroUrl,
            seasonTabs = listOf(
                SeriesSeasonUiModel(
                    name = "Season 1",
                    isSelected = true,
                    episodes = listOf(episode1, episode2, episode3)
                ),
                SeriesSeasonUiModel(
                    name = "Season 2",
                    isSelected = false,
                    episodes = listOf(episode1, episode2, episode3)
                ),
                SeriesSeasonUiModel(
                    name = "Season 3",
                    isSelected = false,
                    episodes = listOf(episode1, episode2, episode3)
                )
            ),
            cast = listOf(
                SeriesCastMemberUiModel(
                    name = "Marcus Thorne",
                    role = "Cmdr. Vance",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6OPszCXCIP_FMO3BJJUrjpCtDNw9aeHYOGyOAXdqF078hDFNrH7KXbaQ7qtipz6aIPLivd8VBBffNMbeAiYIjjWjn5GMb6Xn9iiJz0D2rzhCKi0TBeFrN6tC1IXJkzQyQKJNhTnyokWy9dd-YtN65V7er7RT6hP5jdVBXhtK1xZMjlgrm1bk_FTTmKd8Afu3zPtJCaaC98Z608vav5zhYlkrdA1wKNSTWTpzwMSyDIY3pNQNPFauWf0n-iEu7QsYTAwhCG_zfxz0"
                ),
                SeriesCastMemberUiModel(
                    name = "Elena Rossi",
                    role = "Dr. Sarah Cole",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBExsf-wEzAVjMxasU2ImGhlreqQo9biBSN1yHyAbW8MyuhuppRw9ho7OD3vsbySSJ3kNluEgH1Qun45PmLnZWixZsFU4Qc7UGGJNKMS5Nkm4GZAsKdFvb3z_i1tkCvaXXvGpqmwI0qjFuo1QyjjhYPA5Yp3I8ZhrnDYdQv_GxbhR6Vl3mY1rbxd2BIUEE5oMTwTF-QmJztUEaViZkSGSG2VgVXZ5VAREn4xWE902OH2sysllvXQJQIaj439JIC2_Vg61m0-F-F1Vc"
                ),
                SeriesCastMemberUiModel(
                    name = "Julian Chen",
                    role = "Tech Officer Lin",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA5CFDWsWYO4YxdRoLd2QfH5Su2KLhtj5xSDb8qmzWHvPE888ac_HAAj1wu1uqdFNSncdmmJ-bWsc--h6NYKxVXkhd4vHaFWi0XTJXgsR0F3cBu_l2SynSX4TMNSy5C3XWDurgeSH789byOe1HvoxHCHTJYaSf3OyEbil-NOp9g_9mZ24CIZOI79nx57CRzmooxoswycqssPpfTNkrnoYrrAczt5qbncwLM9NVU442YxyBFisr2Ds9H-CNBOakiCtaKnoJ6npznM7U"
                ),
                SeriesCastMemberUiModel(
                    name = "Sarah Jenkins",
                    role = "Mission Pilot",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBN6_72VggBdNx7ITLvEvIA6OSre5iJI6kQiUVMpKAlYgd8TpT-Jx6DzZwGsGACLnAXOUuzT2R7mx9A9DNZcqi5BF_jSaEdeYpfcBvJttmVPAwiCiq1_PI2BwoZZH_Ccmq2AHV5lQqcYaA2rPkf4e7YLLLgpmVbGjKhncTotQtxiZvmLNzCbLUdlEb7XLgHKfjS6FU6djV9ocOo9bxZ_YtrQj-mMFvYGzCxeFYC8OF0kIV2NN3kQYH8x1X-rYMqu2-d7klJfQdhKHw"
                ),
                SeriesCastMemberUiModel(
                    name = "David Wu",
                    role = "The AI (Voice)",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCnNkjaBc2hU2zJ5hAF8iZZ_ZZvMlU79o4JtPNCP2MEfttpF0fe_BHWsMMl6h3S37FJ1dTLk8AQuvRQ_ggy1u-71xlQWULB76rT8pdZiRE7TkInQ8gwpigs84KNWbTRxVUI7Nia9RPyJeFE7egZqnT46TQWUeN8llWF9EDQ6mpfVLH0vHhKUlko39iDgMnBIequYntugSFgWJQc1jH-AxZ4OpJr_-uZGkwtQ_CVYNV69u9y107gk5BwaUFwPeipe8Bn9I655kyHIuQ"
                ),
                SeriesCastMemberUiModel(
                    name = "Alex Reed",
                    role = "Engineer",
                    imageUrl = null
                )
            )
        )
    }
}
