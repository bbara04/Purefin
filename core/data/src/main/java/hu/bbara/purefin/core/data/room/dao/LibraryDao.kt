package hu.bbara.purefin.core.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.bbara.purefin.core.data.room.LibraryWithContent
import hu.bbara.purefin.core.data.room.entity.LibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Upsert
    suspend fun upsert(library: LibraryEntity)

    @Upsert
    suspend fun upsertAll(libraries: List<LibraryEntity>)

    @Query("SELECT * FROM library")
    fun observeAll(): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM library")
    fun observeAllWithContent(): Flow<List<LibraryWithContent>>

    @Query("SELECT * FROM library")
    suspend fun getAll(): List<LibraryEntity>

    @Query("DELETE FROM library")
    suspend fun deleteAll()
}
