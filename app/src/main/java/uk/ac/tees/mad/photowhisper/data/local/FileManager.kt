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
        val fileName = "${UUID.randomUUID()}_thumb.jpg"
        val file = File(thumbnailDirectory, fileName)

        val bitmap = BitmapFactory.decodeFile(photoPath)
        val thumbnail = Bitmap.createScaledBitmap(bitmap, 200, 200, true)

        FileOutputStream(file).use { output ->
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, output)
        }

        bitmap.recycle()
        thumbnail.recycle()

        return file.absolutePath
    }

    fun saveAudio(sourcePath: String): String {
        val fileName = "${UUID.randomUUID()}.m4a"
        val file = File(audioDirectory, fileName)

        File(sourcePath).copyTo(file, overwrite = true)

        return file.absolutePath
    }

    fun deleteFile(path: String) {
        File(path).delete()
    }

    fun clearCache() {
        thumbnailDirectory.listFiles()?.forEach { it.delete() }
    }

    fun getPhotoFile(path: String): File? {
        val file = File(path)
        return if (file.exists()) file else null
    }
}