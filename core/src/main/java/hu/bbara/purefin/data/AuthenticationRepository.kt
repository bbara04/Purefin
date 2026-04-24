package hu.bbara.purefin.data

interface AuthenticationRepository {
    suspend fun login(url: String, username: String, password: String): Boolean
}
