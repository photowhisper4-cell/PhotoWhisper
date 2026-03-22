package uk.ac.tees.mad.photowhisper.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoryDto(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("audio_url")
    val audioUrl: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("date_captured")
    val dateCaptured: Long,
    @SerialName("location")
    val location: String? = null,
    @SerialName("camera_info")
    val cameraInfo: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)