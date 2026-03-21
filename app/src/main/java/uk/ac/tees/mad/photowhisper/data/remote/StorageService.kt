package uk.ac.tees.mad.photowhisper.data.remote

import io.github.jan.supabase.storage.storage
import uk.ac.tees.mad.photowhisper.data.remote.SupabaseClient
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
            val bytes = file.readBytes()
            val fileName = "$userId/$photoId.jpg"

            storage.from(PHOTOS_BUCKET).upload(fileName, bytes)

            val publicUrl = storage.from(PHOTOS_BUCKET).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAudio(userId: String, audioPath: String, audioId: String): Result<String> {
        return try {
            val file = File(audioPath)
            val bytes = file.readBytes()
            val fileName = "$userId/$audioId.m4a"

            storage.from(AUDIO_BUCKET).upload(fileName, bytes)

            val publicUrl = storage.from(AUDIO_BUCKET).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadPhoto(photoUrl: String, localPath: String): Result<Unit> {
        return try {
            val fileName = photoUrl.substringAfterLast("/")
            val bytes = storage.from(PHOTOS_BUCKET).downloadAuthenticated(fileName)
            File(localPath).writeBytes(bytes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAudio(audioUrl: String, localPath: String): Result<Unit> {
        return try {
            val fileName = audioUrl.substringAfterLast("/")
            val bytes = storage.from(AUDIO_BUCKET).downloadAuthenticated(fileName)
            File(localPath).writeBytes(bytes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}