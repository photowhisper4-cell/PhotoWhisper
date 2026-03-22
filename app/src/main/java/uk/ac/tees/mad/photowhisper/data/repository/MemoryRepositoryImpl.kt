package uk.ac.tees.mad.photowhisper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.photowhisper.data.local.dao.MemoryDao
import uk.ac.tees.mad.photowhisper.data.mapper.toDomain
import uk.ac.tees.mad.photowhisper.data.mapper.toEntity
import uk.ac.tees.mad.photowhisper.data.remote.SyncService
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.repository.MemoryRepository

class MemoryRepositoryImpl(
    private val memoryDao: MemoryDao,
    private val syncService: SyncService
) : MemoryRepository {

    override fun getAllMemories(userId: String): Flow<List<Memory>> {
        return memoryDao.getAllMemoriesForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMemoryById(memoryId: String): Memory? {
        return memoryDao.getMemoryById(memoryId)?.toDomain()
    }

    override suspend fun saveMemory(memory: Memory) {
        memoryDao.insertMemory(memory.toEntity())

        try {
            syncService.syncMemoryToCloud(memory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteMemory(memoryId: String) {
        memoryDao.deleteMemory(memoryId)
    }

    override suspend fun syncMemories(userId: String) {
        try {
            syncService.syncMemoriesFromCloud(userId)
            syncService.syncAllUnsyncedMemories(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}