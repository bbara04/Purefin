package hu.bbara.purefin.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.data.local.room.MovieEntity
import java.util.UUID

@Dao
interface MovieDao {
    @Upsert
    suspend fun upsert(movie: MovieEntity)

    @Upsert
    suspend fun upsertAll(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies")
    suspend fun getAll(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getById(id: UUID): MovieEntity?

    @Query("DELETE FROM movies")
    suspend fun clear()
}
