package uk.ac.tees.mad.photowhisper.data.remote

import io.github.jan.supabase.storage.storage
import java.io.File

class StorageService(
    private val supabaseClient: SupabaseClient
) {
    private val storage = supabaseClient.client.storage

    private companion object {
        const val PHOTOS_BUCKET = "photos"
        const val AUDIO_BUCKET = "audio"
    }

    suspend fun uploadPhoto(userId: String, photoPath: String, photoId: String): Result<String> {
        return try {
            val file = File(photoPath)
            if (!file.exists()) {
                return Result.failure(Exception("Photo file not found: $photoPath"))
            }

            val bytes = file.readBytes()
            val fileName = "$userId/$photoId.jpg"

            storage.from(PHOTOS_BUCKET).upload(
                path = fileName,
                data = bytes,
                upsert = true
            )

            val publicUrl = storage.from(PHOTOS_BUCKET).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun uploadAudio(userId: String, audioPath: String, audioId: String): Result<String> {
        return try {
            val file = File(audioPath)
            if (!file.exists()) {
                return Result.failure(Exception("Audio file not found: $audioPath"))
            }

            val bytes = file.readBytes()
            val extension = when {
                audioPath.endsWith(".m4a", ignoreCase = true) -> "m4a"
                audioPath.endsWith(".mp4", ignoreCase = true) -> "mp4"
                audioPath.endsWith(".aac", ignoreCase = true) -> "aac"
                else -> "m4a"
            }
            val fileName = "$userId/$audioId.$extension"

            storage.from(AUDIO_BUCKET).upload(
                path = fileName,
                data = bytes,
                upsert = true
            )

            val publicUrl = storage.from(AUDIO_BUCKET).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun downloadPhoto(photoUrl: String, localPath: String): Result<Unit> {
        return try {
            val pathParts = photoUrl.split("/")
            val userId = pathParts[pathParts.size - 2]
            val fileName = pathParts.last()
            val fullPath = "$userId/$fileName"

            val bytes = storage.from(PHOTOS_BUCKET).downloadAuthenticated(fullPath)
            File(localPath).apply {
                parentFile?.mkdirs()
                writeBytes(bytes)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun downloadAudio(audioUrl: String, localPath: String): Result<Unit> {
        return try {
            val pathParts = audioUrl.split("/")
            val userId = pathParts[pathParts.size - 2]
            val fileName = pathParts.last()
            val fullPath = "$userId/$fileName"

            val bytes = storage.from(AUDIO_BUCKET).downloadAuthenticated(fullPath)
            File(localPath).apply {
                parentFile?.mkdirs()
                writeBytes(bytes)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}