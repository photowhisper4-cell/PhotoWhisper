package uk.ac.tees.mad.photowhisper.data.remote

import kotlinx.coroutines.flow.first
import uk.ac.tees.mad.photowhisper.data.local.FileManager
import uk.ac.tees.mad.photowhisper.data.local.dao.MemoryDao
import uk.ac.tees.mad.photowhisper.data.mapper.toDomain
import uk.ac.tees.mad.photowhisper.data.mapper.toEntity
import uk.ac.tees.mad.photowhisper.data.remote.dto.MemoryDto
import uk.ac.tees.mad.photowhisper.domain.model.Memory

class SyncService(
    private val storageService: StorageService,
    private val databaseService: DatabaseService,
    private val memoryDao: MemoryDao,
    private val fileManager: FileManager
) {
    suspend fun syncMemoryToCloud(memory: Memory): Result<Memory> {
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

            val memoryDto = MemoryDto(
                id = memory.id,
                userId = memory.userId,
                photoUrl = photoUrl,
                audioUrl = audioUrl,
                thumbnailUrl = memory.thumbnailUrl,
                dateCaptured = memory.dateCaptured,
                location = memory.location,
                cameraInfo = memory.cameraInfo
            )

            databaseService.insertMemory(memoryDto).getOrThrow()

            val syncedMemory = memory.copy(
                photoUrl = photoUrl,
                audioUrl = audioUrl,
                isSynced = true
            )

            memoryDao.insertMemory(syncedMemory.toEntity())

            Result.success(syncedMemory)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun syncMemoriesFromCloud(userId: String): Result<List<Memory>> {
        return try {
            val cloudMemories = databaseService.getMemoriesForUser(userId).getOrThrow()
            val syncedMemories = mutableListOf<Memory>()

            cloudMemories.forEach { dto ->
                val existingMemory = memoryDao.getMemoryById(dto.id)

                val localPhotoPath = if (existingMemory?.localPhotoPath != null && fileManager.fileExists(existingMemory.localPhotoPath)) {
                    existingMemory.localPhotoPath
                } else if (dto.photoUrl.isNotEmpty()) {
                    val newPath = "${fileManager.getPhotoDirectory()}/${dto.id}.jpg"
                    val downloadResult = storageService.downloadPhoto(dto.photoUrl, newPath)
                    if (downloadResult.isSuccess) newPath else null
                } else {
                    null
                }

                val localAudioPath = if (existingMemory?.localAudioPath != null && fileManager.fileExists(existingMemory.localAudioPath)) {
                    existingMemory.localAudioPath
                } else if (dto.audioUrl.isNotEmpty()) {
                    val extension = dto.audioUrl.substringAfterLast(".").substringBefore("?")
                    val newPath = "${fileManager.getAudioDirectory()}/${dto.id}.$extension"
                    val downloadResult = storageService.downloadAudio(dto.audioUrl, newPath)
                    if (downloadResult.isSuccess) newPath else null
                } else {
                    null
                }

                val thumbnailPath = if (localPhotoPath != null) {
                    val thumbPath = fileManager.getThumbnailPath(dto.id)
                    if (!fileManager.fileExists(thumbPath)) {
                        fileManager.saveThumbnail(localPhotoPath)
                    } else {
                        thumbPath
                    }
                } else {
                    null
                }

                val memory = Memory(
                    id = dto.id,
                    userId = dto.userId,
                    photoUrl = dto.photoUrl,
                    audioUrl = dto.audioUrl,
                    thumbnailUrl = thumbnailPath,
                    dateCaptured = dto.dateCaptured,
                    location = dto.location,
                    cameraInfo = dto.cameraInfo,
                    isSynced = true,
                    localPhotoPath = localPhotoPath,
                    localAudioPath = localAudioPath
                )

                memoryDao.insertMemory(memory.toEntity())
                syncedMemories.add(memory)
            }

            Result.success(syncedMemories)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun syncAllUnsyncedMemories(userId: String): Result<Int> {
        return try {
            val allMemories = memoryDao.getAllMemoriesForUser(userId).first()
            val unsyncedMemories = allMemories
                .filter { !it.isSynced }
                .map { it.toDomain() }

            var syncedCount = 0
            unsyncedMemories.forEach { memory ->
                val result = syncMemoryToCloud(memory)
                if (result.isSuccess) {
                    syncedCount++
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}