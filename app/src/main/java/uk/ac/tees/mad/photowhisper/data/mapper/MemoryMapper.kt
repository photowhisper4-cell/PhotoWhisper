package uk.ac.tees.mad.photowhisper.data.mapper

import uk.ac.tees.mad.photowhisper.data.local.entity.MemoryEntity
import uk.ac.tees.mad.photowhisper.domain.model.Memory

fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = id,
        userId = userId,
        photoUrl = photoUrl,
        audioUrl = audioUrl,
        thumbnailUrl = thumbnailUrl,
        dateCaptured = dateCaptured,
        location = location,
        cameraInfo = cameraInfo,
        isSynced = isSynced,
        localPhotoPath = localPhotoPath,
        localAudioPath = localAudioPath
    )
}

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        userId = userId,
        photoUrl = photoUrl,
        audioUrl = audioUrl,
        thumbnailUrl = thumbnailUrl,
        dateCaptured = dateCaptured,
        location = location,
        cameraInfo = cameraInfo,
        isSynced = isSynced,
        localPhotoPath = localPhotoPath,
        localAudioPath = localAudioPath
    )
}