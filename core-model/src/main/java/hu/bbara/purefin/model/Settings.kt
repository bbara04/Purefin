package hu.bbara.purefin.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val numberSettings: Map<String, Double> = emptyMap(),
    val booleanSettings: Map<String, Boolean> = emptyMap(),
    val stringSettings: Map<String, String> = emptyMap()
)
