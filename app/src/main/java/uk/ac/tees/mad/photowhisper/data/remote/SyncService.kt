package uk.ac.tees.mad.photowhisper.data.remote

import uk.ac.tees.mad.photowhisper.data.local.dao.MemoryDao
import uk.ac.tees.mad.photowhisper.data.mapper.toEntity
import uk.ac.tees.mad.photowhisper.domain.model.Memory

class SyncService(
    private val storageService: StorageService,
    private val memoryDao: MemoryDao
) {
    suspend fun syncMemory(memory: Memory): Result<Memory> {
        return try {
            var photoUrl = memory.photoUrl
            var audioUrl = memory.audioUrl

            if (memory.localPhotoPath != null && photoUrl.isEmpty()) {
                val photoResult = storageService.uploadPhoto(
                    memory.userId,
                    memory.localPhotoPath,
                    memory.id
                )
                photoUrl = photoResult.getOrThrow()
            }

            if (memory.localAudioPath != null && audioUrl.isEmpty()) {
                val audioResult = storageService.uploadAudio(
                    memory.userId,
                    memory.localAudioPath,
                    memory.id
                )
                audioUrl = audioResult.getOrThrow()
            }

            val syncedMemory = memory.copy(
                photoUrl = photoUrl,
                audioUrl = audioUrl,
                isSynced = true
            )

            memoryDao.insertMemory(syncedMemory.toEntity())

            Result.success(syncedMemory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllUnsyncedMemories(userId: String): Result<Int> {
        return try {
            val unsyncedMemories = memoryDao.getAllMemoriesForUser(userId)
            var syncedCount = 0

            unsyncedMemories.collect { memories ->
                memories.filter { !it.isSynced }.forEach { memoryEntity ->
                    val memory = Memory(
                        id = memoryEntity.id,
                        userId = memoryEntity.userId,
                        photoUrl = memoryEntity.photoUrl,
                        audioUrl = memoryEntity.audioUrl,
                        thumbnailUrl = memoryEntity.thumbnailUrl,
                        dateCaptured = memoryEntity.dateCaptured,
                        location = memoryEntity.location,
                        cameraInfo = memoryEntity.cameraInfo,
                        isSynced = memoryEntity.isSynced,
                        localPhotoPath = memoryEntity.localPhotoPath,
                        localAudioPath = memoryEntity.localAudioPath
                    )

                    val result = syncMemory(memory)
                    if (result.isSuccess) {
                        syncedCount++
                    }
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}