package hu.bbara.purefin.core.data

interface AuthenticationRepository {
    suspend fun login(url: String, username: String, password: String): Boolean
}
