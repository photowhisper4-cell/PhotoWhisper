package uk.ac.tees.mad.photowhisper.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileManager(private val context: Context) {

    private val photoDirectory: File by lazy {
        File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
    }

    private val audioDirectory: File by lazy {
        File(context.filesDir, "audio").apply {
            if (!exists()) mkdirs()
        }
    }

    private val thumbnailDirectory: File by lazy {
        File(context.cacheDir, "thumbnails").apply {
            if (!exists()) mkdirs()
        }
    }

    fun getPhotoDirectory(): String = photoDirectory.absolutePath

    fun getAudioDirectory(): String = audioDirectory.absolutePath

    fun savePhoto(uri: Uri): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(photoDirectory, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    fun savePhoto(bitmap: Bitmap): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(photoDirectory, fileName)

        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }

        return file.absolutePath
    }

    fun saveThumbnail(photoPath: String): String {
        val photoFile = File(photoPath)
        val fileName = "${photoFile.nameWithoutExtension}_thumb.jpg"
        val file = File(thumbnailDirectory, fileName)

        if (file.exists()) {
            return file.absolutePath
        }

        val bitmap = BitmapFactory.decodeFile(photoPath)
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val thumbnailWidth = 200
        val thumbnailHeight = (thumbnailWidth / aspectRatio).toInt()
        val thumbnail = Bitmap.createScaledBitmap(bitmap, thumbnailWidth, thumbnailHeight, true)

        FileOutputStream(file).use { output ->
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, output)
        }

        bitmap.recycle()
        thumbnail.recycle()

        return file.absolutePath
    }

    fun getThumbnailPath(memoryId: String): String {
        return File(thumbnailDirectory, "${memoryId}_thumb.jpg").absolutePath
    }

    fun saveAudio(sourcePath: String): String {
        val sourceFile = File(sourcePath)
        val fileName = "${UUID.randomUUID()}.${sourceFile.extension}"
        val file = File(audioDirectory, fileName)

        sourceFile.copyTo(file, overwrite = true)

        return file.absolutePath
    }

    fun deleteFile(path: String) {
        File(path).delete()
    }

    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    fun clearCache() {
        thumbnailDirectory.listFiles()?.forEach { it.delete() }
    }

    fun getPhotoFile(fileName: String): File? {
        val file = File(photoDirectory, fileName)
        return if (file.exists()) file else null
    }

    fun getAudioFile(fileName: String): File? {
        val file = File(audioDirectory, fileName)
        return if (file.exists()) file else null
    }
}