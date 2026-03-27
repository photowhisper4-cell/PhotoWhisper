package uk.ac.tees.mad.photowhisper.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.photowhisper.domain.model.Memory

interface MemoryRepository {
    fun getAllMemories(userId: String): Flow<List<Memory>>
    suspend fun getMemoryById(memoryId: String): Memory?
    suspend fun saveMemory(memory: Memory)
    suspend fun deleteMemory(memoryId: String)
    suspend fun deleteMemoryWithFiles(memoryId: String)
    suspend fun syncMemories(userId: String)
}