package uk.ac.tees.mad.photowhisper.domain.model

data class Memory(
    val id: String,
    val userId: String,
    val photoUrl: String,
    val audioUrl: String,
    val thumbnailUrl: String?,
    val dateCaptured: Long,
    val location: String?,
    val cameraInfo: String?,
    val isSynced: Boolean = false,
    val localPhotoPath: String?,
    val localAudioPath: String?
)