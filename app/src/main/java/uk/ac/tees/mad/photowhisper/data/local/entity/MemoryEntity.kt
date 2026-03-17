package uk.ac.tees.mad.photowhisper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val photoUrl: String,
    val audioUrl: String,
    val thumbnailUrl: String?,
    val dateCaptured: Long,
    val location: String?,
    val cameraInfo: String?,
    val isSynced: Boolean,
    val localPhotoPath: String?,
    val localAudioPath: String?
)