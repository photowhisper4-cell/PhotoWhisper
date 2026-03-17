package uk.ac.tees.mad.photowhisper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.photowhisper.data.local.entity.MemoryEntity

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE userId = :userId ORDER BY dateCaptured DESC")
    fun getAllMemoriesForUser(userId: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :memoryId")
    suspend fun getMemoryById(memoryId: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Query("DELETE FROM memories WHERE id = :memoryId")
    suspend fun deleteMemory(memoryId: String)

    @Query("DELETE FROM memories WHERE userId = :userId")
    suspend fun deleteAllMemoriesForUser(userId: String)
}