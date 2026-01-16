package hu.bbara.purefin.session

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val accessToken: String,
    val url: String,
    val loggedIn: Boolean
)
